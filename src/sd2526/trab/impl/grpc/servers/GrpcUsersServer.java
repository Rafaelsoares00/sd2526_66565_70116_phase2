package sd2526.trab.impl.grpc.servers;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
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
import sd2526.trab.api.java.Users;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;

public class GrpcUsersServer extends AbstractGrpcServer {
public static final int PORT = 13456;
	
	private static Logger Log = Logger.getLogger(GrpcUsersServer.class.getName());

	public GrpcUsersServer() throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, SSLException {
		super( Log, Users.SERVICE_NAME, PORT);
	}
	
	@Override
	protected List<GrpcController> controllers(String uri) {
		return List.of( new GrpcUsersController(), new GrpcAdminUsersController() );
	}
	
	public static void main(String[] args) throws KeyStoreException, UnrecoverableKeyException, UnknownHostException, NoSuchAlgorithmException, SSLException {
        try {
			new GrpcUsersServer().start();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (KeyStoreException | UnrecoverableKeyException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
	
	
}
