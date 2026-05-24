package sd2526.trab.impl.grpc.servers;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.logging.Logger;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import sd2526.trab.impl.discovery.Discovery;
import sd2526.trab.impl.java.servers.AbstractServer;
import sd2526.trab.impl.utils.IP;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;


public abstract class AbstractGrpcServer extends AbstractServer {
	public static final String SERVER_BASE_URI = "grpc://%s:%s%s";

	public static final String GRPC_CTX = "/grpc";

	protected final Server server;

	protected AbstractGrpcServer(Logger log, String service, int port) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, SSLException {
		super(log, service, String.format(SERVER_BASE_URI, IP.hostname(), port, GRPC_CTX));

        String keyStoreFilename = System.getProperty("javax.net.ssl.keyStore");
        String keyStorePassword = System.getProperty("javax.net.ssl.keyStorePassword");

        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());

        try(FileInputStream input = new FileInputStream(keyStoreFilename)) {
            keystore.load(input, keyStorePassword.toCharArray());
        } catch (CertificateException | NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException(e);
        }

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keystore, keyStorePassword.toCharArray());
        SslContext context = GrpcSslContexts.configure(SslContextBuilder.forServer(keyManagerFactory)).build();

        var builder = NettyServerBuilder.forPort(port).sslContext(context);

        for (var controller : controllers(super.serverURI)) {
            builder.addService(controller);
        }

        server = builder.build();
	}

	protected abstract List<GrpcController> controllers( String uri );
	
	protected void start() throws IOException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
		
		Discovery.getInstance().announce(serviceName(), super.serverURI);

        Log.info(String.format("%s gRPC Server ready @ %s\n", service, serverURI));

		server.start();
		Runtime.getRuntime().addShutdownHook(new Thread( () -> {
			System.err.println("*** shutting down gRPC server since JVM is shutting down");
			server.shutdownNow();
			System.err.println("*** server shut down");
		}));
	}
	
}
