package sd2526.trab.impl.zoho.servers;

import java.net.URI;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import sd2526.trab.api.java.Messages;
import sd2526.trab.impl.discovery.Discovery;
import sd2526.trab.impl.utils.IP;
import sd2526.trab.impl.zoho.Zoho;

public class ZohoMessagesServer {
    public static final int PORT = 4568;
    private static final String SERVER_BASE_URI = "https://%s:%s%s";
    private static final String REST_CTX = "/rest";
    private static final String INETADDR_ANY = "0.0.0.0";
    private static Logger Log = Logger.getLogger(ZohoMessagesServer.class.getName());

    public static void main(String[] args) throws Exception {
        boolean freshStart = Boolean.parseBoolean(args[0]);

        if (freshStart)
            Zoho.getInstance().deleteAllMessages();

        String serverURI = String.format(SERVER_BASE_URI, IP.hostname(), PORT, REST_CTX);

        ResourceConfig config = new ResourceConfig();
        config.register(ZohoMessagesResource.class);

        JdkHttpServerFactory.createHttpServer(URI.create(serverURI.replace(IP.hostname(), INETADDR_ANY)), config, SSLContext.getDefault()
        );

        Discovery.getInstance().announce(Messages.SERVICE_NAME, serverURI);
        Log.info("ZohoMessages Server ready @ " + serverURI);
    }
}