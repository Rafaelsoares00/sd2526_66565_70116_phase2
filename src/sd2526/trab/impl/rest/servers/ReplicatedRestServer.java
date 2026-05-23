package sd2526.trab.impl.rest.servers;

import org.glassfish.jersey.server.ResourceConfig;
import sd2526.trab.api.java.Messages;
import sd2526.trab.impl.kafka.ReplicationManager;

import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

public class ReplicatedRestServer extends AbstractRestServer {
    public static final int PORT = 4569;

    private static Logger Log = Logger.getLogger(RestMessagesServer.class.getName());

    ReplicatedRestServer() {
        super(Log, Messages.SERVICE_NAME, PORT);
    }

    @Override
    void registerResources(ResourceConfig config) {
        var resource = new ReplicatedRestMessagesResource();
        config.register(resource);
        config.register(new ReplicationManager.VersionHeaderHandler());
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
        new ReplicatedRestServer().start();
    }
}
