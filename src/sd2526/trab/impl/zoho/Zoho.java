package sd2526.trab.impl.zoho;

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;

import sd2526.trab.api.Message;
import sd2526.trab.impl.zoho.zohoAPI.ZohoServiceFactory;
import sd2526.trab.impl.zoho.zohoAPI.ZohoTokenManager;
import sd2526.trab.impl.zoho.zohoAPI.msgs.*;
import sd2526.trab.impl.utils.JSON;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Zoho {
	static final String MAIL_API_BASE = "https://mail.zoho.eu/api";

	static final String CLIENT_ID     = "1000.MM3PLMLH0R62S22XVFPZHA712F082W";
    static final String CLIENT_SECRET = "a77bcc254647c23e558ba9dd16e30627f96f9770b6";
    static final String REFRESH_TOKEN = "1000.e41fe868ec8ac8e8a98ee5cfd3874de1.70b8b915463d91417bbf08dc40062fb0";

	private static final String ACCOUNTS = "/accounts";
    private static final String MESSAGES = "/messages";
    private static final String FOLDERS = "/folders/";
    private static final String SEPARATOR = "\n------\n";

    final OAuth20Service service;
    final ZohoTokenManager tokenManager;

    static Zoho instance;
    
    private Zoho() {
    	service = ZohoServiceFactory.buildService(CLIENT_ID, CLIENT_SECRET);
        tokenManager = new ZohoTokenManager(service, REFRESH_TOKEN);
    }
 
    synchronized public static Zoho getInstance() {
    	if( instance == null )
    		instance = new Zoho();
    	return instance;
    }

    public ZohoAccount getAccount() throws Exception {
        var accessToken = new OAuth2AccessToken( tokenManager.getValidAccessToken() );

        OAuthRequest request = new OAuthRequest(Verb.GET, MAIL_API_BASE + ACCOUNTS);
        service.signRequest(accessToken, request);

        try (Response response = service.execute(request)) {
        	if( response.isSuccessful() ) {
        		var body = response.getBody();
        		var data = JSON.decode(body, ZohoAccountReply.class).data();
        		if (data == null || data.isEmpty()) return null;
        		return data.get(0);
        	}
        	else {
        		System.err.println( response.getCode() + "/" + response.getBody() );
        		return null;
        	}
        }
    }

    public String sendMessage(String messageId, String sender, String destination, long creationTime, String content) throws Exception {
        ZohoAccount account = getAccount();
        String accountID = account.accountId();
        String mailbox = account.mailboxAddress();

        var accessToken = new OAuth2AccessToken(tokenManager.getValidAccessToken());
        OAuthRequest request = new OAuthRequest(Verb.POST, MAIL_API_BASE + ACCOUNTS + "/" + accountID + MESSAGES);
        request.addHeader("Content-Type", "application/json; charset=utf-8");
        request.addHeader("Accept", "application/json");

        String fullContent = content + SEPARATOR
                + "id=" + messageId + "\n"
                + "sender=" + sender + "\n"
                + "destination=" + destination + "\n"
                + "creationTime=" + creationTime;

        var body = JSON.encode(Map.of("fromAddress", mailbox,
                "toAddress", mailbox,
                "subject", messageId,
                "content", fullContent));

        request.setPayload(body);
        service.signRequest(accessToken, request);

        try (Response response = service.execute(request)) {
            if (!response.isSuccessful())
                throw new RuntimeException(response.getCode() + ": " + response.getBody());

            return response.getBody();
        }
    }

    public List<ZohoMessage> getAllMessages() throws Exception {
        String accountID = getAccount().accountId();
        var accessToken = new OAuth2AccessToken(tokenManager.getValidAccessToken());
        OAuthRequest request = new OAuthRequest(Verb.GET, MAIL_API_BASE + ACCOUNTS + "/" + accountID + MESSAGES + "/view");
        request.addHeader("Accept", "application/json");
        service.signRequest(accessToken, request);
        try (Response response = service.execute(request)) {
            if (!response.isSuccessful())
                throw new RuntimeException(response.getCode() + ": " + response.getBody());

            var body = response.getBody();
            var data = JSON.decode(body, ZohoMessageReply.class).data();
            if (data == null || data.isEmpty()) return null;
            return data;
        }
    }

    public ZohoMessage getMessage(String messageID) throws Exception {
        String accountID = getAccount().accountId();
        var accessToken = new OAuth2AccessToken(tokenManager.getValidAccessToken());
        OAuthRequest request = new OAuthRequest(Verb.GET, MAIL_API_BASE + ACCOUNTS + "/" + accountID + MESSAGES + "/view");
        request.addHeader("Accept", "application/json");
        service.signRequest(accessToken, request);
        try (Response response = service.execute(request)) {
            if (!response.isSuccessful())
                throw new RuntimeException(response.getCode() + ": " + response.getBody());

            var body = response.getBody();
            var data = JSON.decode(body, ZohoMessageReply.class).data();
            if (data == null || data.isEmpty()) return null;
            for(ZohoMessage message : data){
                if (message != null && message.subject().contains(messageID))
                    return message;
            }
            return null;
        }
    }

    public void deleteMessage(String messageId, String folderId) throws Exception {
        String accountID = getAccount().accountId();
        var accessToken = new OAuth2AccessToken(tokenManager.getValidAccessToken());
        OAuthRequest request = new OAuthRequest(Verb.DELETE, MAIL_API_BASE + ACCOUNTS + "/" + accountID + FOLDERS + folderId + MESSAGES + "/" + messageId);
        service.signRequest(accessToken, request);
        try (Response response = service.execute(request)) {
            if (!response.isSuccessful())
                throw new RuntimeException(response.getCode() + ": " + response.getBody());
        }
    }

    public Message getParsedMessage(String accountId, String folderId, String messageId) throws Exception {
        String content = getMessageContent(accountId, folderId, messageId);

        if (content == null)
            return null;

        int separator = content.lastIndexOf(SEPARATOR);

        return null;
    }

    /*To allow for testing this service automatically, using the Tester, it is necessary to start with a clean state, i.e.,
     *  with an empty mailbox. To achieve this, the Tester will pass as the first parameter of this Messages server the value true
     *  to indicate that the previous state should be ignored.*/
    public void deleteAllMessages() throws Exception {
        var msgs = getAllMessages();
        if (msgs != null)
            for (ZohoMessage msg : msgs)
                deleteMessage(msg.messageId(), msg.folderId());
    }

    //If the Tester passes the value false, the saved state should be used by the server.
//    public List<ZohoMessage> getAllStoredMessages() throws Exception {
//        var msgs = getAllMessages();
//        if (msgs != null)
//            return msgs;
//        else
//            return new ArrayList<>();
//    } i actually dont think this is needed

    private String getMessageContent(String accountId, String folderId, String messageId) throws Exception {
        var accessToken = new OAuth2AccessToken(tokenManager.getValidAccessToken());
        OAuthRequest request = new OAuthRequest(Verb.GET, MAIL_API_BASE + ACCOUNTS + "/" + accountId + FOLDERS + folderId + MESSAGES + "/" + messageId + "/content");
        request.addHeader("Accept", "application/json");
        service.signRequest(accessToken, request);

        try (Response response = service.execute(request)) {
            if (!response.isSuccessful())
                throw new RuntimeException(response.getCode() + ": " + response.getBody());
            var reply = JSON.decode(response.getBody(), ZohoContentReply.class);
            return reply.data().content();
        }
    }
}