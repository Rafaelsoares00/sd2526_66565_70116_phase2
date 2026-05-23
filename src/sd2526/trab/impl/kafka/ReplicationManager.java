package sd2526.trab.impl.kafka;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import sd2526.trab.api.java.Result;
import sd2526.trab.api.rest.RestMessages;
import sd2526.trab.impl.java.servers.JavaMessages;
import sd2526.trab.impl.rest.servers.ReplicatedRestMessagesResource;
import sd2526.trab.impl.utils.IP;
import sd2526.trab.impl.utils.JSON;
import sd2526.trab.impl.utils.SyncPoint;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class ReplicationManager {
    private static Logger log = Logger.getLogger(ReplicationManager.class.getName());
    private static KafkaPublisher kafkaPublisher;
    private static KafkaSubscriber sub;
    private static final String ADDRESSES = "kafka:9092,localhost:9092";
    private final String topic = "messages"+ IP.domain();
    private static final JavaMessages impl = JavaMessages.getInstance();
    public static volatile long currentVersion = -1;
    private final Map<String, Long> domainLog = new ConcurrentHashMap<>();

    public ReplicationManager() {
        KafkaUtils.createTopic(topic);


        synchronized (ReplicationManager.class) {


            System.out.println("[KAFKA-START] domain=" + IP.domain()
                    + " topic=" + topic
                    + " replica=" + IP.hostAddress());

            if (kafkaPublisher == null) {
                kafkaPublisher = KafkaPublisher.createPublisher(ADDRESSES);
            }
            if (sub == null) {
                sub = KafkaSubscriber.createSubscriber(ADDRESSES, List.of(topic));
                sub.start(record -> {
                    System.out.println("[KAFKA-CONSUME] topic=" + record.topic()
                            + " offset=" + record.offset()
                            + " value=" + record.value());
                    var op = JSON.decode(record.value(), Operations.class);
                    var result = apply(op);
                    SyncPoint.getSyncPoint().setResult(record.offset(), JSON.encode(result));
                    currentVersion = record.offset();
                });
            }

            domainLog.put(IP.domain(), -1L);


        }
    }
        private Object apply (Operations op){
            return switch (op.type()) {
                case "POST" -> impl.postMessage(op.pwd(), op.msg()).value();

                case "REMOVE" ->{
                    impl.removeInboxMessage(op.name(), op.mid(), op.pwd());
                    yield null;
                }
                case "DELETE" -> {
                    impl.deleteMessage(op.name(), op.mid(), op.pwd());
                    yield null;
                }
                case "REMOTE_POST" -> {
                    long last = domainLog.get(op.domain());
                    if(op.domainVersion() <= last){
                        yield null;
                    }
                    domainLog.put(op.domain(), op.domainVersion());
                    impl.remotePostMessage(op.msg());
                    yield null;
                }
                case "REMOTE_DELETE" -> {
                    impl.remoteDeleteMessage(op.mid());
                    yield null;
                }
                case "REMOTE_DELETE_INBOX" -> {
                    impl.remoteDeleteUserInbox(op.name());
                    yield null;
                }


                default -> throw new RuntimeException();
            };
        }

    public void syncRead() {
        Long clientVersion = VersionHeaderHandler.version.get();

        if (clientVersion != null)
            SyncPoint.getSyncPoint().waitForVersion(clientVersion);

        VersionHeaderHandler.version.set(currentVersion);
    }

    public <T> T submit(Operations op, Class<T> resultClass) {
        String json = JSON.encode(op);

        long offset = kafkaPublisher.publish(topic, json);

        if (offset < 0)
            throw new RuntimeException("Failed to publish operation to Kafka");

        String resultJson = SyncPoint.getSyncPoint().waitForResult(offset);

        VersionHeaderHandler.version.set(offset);

        if (resultClass == Void.class || resultJson == null)
            return null;

        return JSON.decode(resultJson, resultClass);
    }

    @Provider
    public static class VersionHeaderHandler implements ContainerResponseFilter, ContainerRequestFilter {
        @Override
        public void filter(ContainerRequestContext reqCtx) throws IOException {
            String value = reqCtx.getHeaderString(RestMessages.HEADER_VERSION);
            if( value != null && ! value.isEmpty()) {
                version.set( Long.valueOf( value ) );
            }
        }

        @Override
        public void filter(ContainerRequestContext reqCtx, ContainerResponseContext resCtx) throws IOException {
            var value = version.get();
            if( value != null ) {
                resCtx.getHeaders().add(RestMessages.HEADER_VERSION, Long.toString( value ));
            }
        }

        public static final ThreadLocal<Long> version = new ThreadLocal<>();
    }
}
