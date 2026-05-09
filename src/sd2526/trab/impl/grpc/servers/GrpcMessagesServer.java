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
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import sd2526.trab.api.java.Messages;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;

public class GrpcMessagesServer extends AbstractGrpcServer {
public static final int PORT = 14567;
	
	private static Logger Log = Logger.getLogger(GrpcMessagesServer.class.getName());

	public GrpcMessagesServer() throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, SSLException {
		super( Log, Messages.SERVICE_NAME, PORT);
	}
	
	@Override
	protected List<GrpcController> controllers(String uri) {
		return List.of( new GrpcMessagesController(), new GrpcAdminMessagesController() );
	}
	
	public static void main(String[] args) throws KeyStoreException, IOException, UnrecoverableKeyException, NoSuchAlgorithmException {
		try {
			new GrpcMessagesServer().start();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (KeyStoreException | UnrecoverableKeyException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
