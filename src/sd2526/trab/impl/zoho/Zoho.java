package sd2526.trab.impl.zoho;

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;

import sd2526.trab.api.Message;
import sd2526.trab.api.java.Result;
import sd2526.trab.impl.zoho.zohoAPI.ZohoServiceFactory;
import sd2526.trab.impl.zoho.zohoAPI.ZohoTokenManager;
import sd2526.trab.impl.zoho.zohoAPI.msgs.*;
import sd2526.trab.impl.utils.JSON;

import java.util.*;

public class Zoho {
	static final String MAIL_API_BASE = "https://mail.zoho.eu/api";

	static final String CLIENT_ID     = "1000.MM3PLMLH0R62S22XVFPZHA712F082W";
    static final String CLIENT_SECRET = "a77bcc254647c23e558ba9dd16e30627f96f9770b6";
    static final String REFRESH_TOKEN = "1000.da5e1bc8db29b43818e8ed2c448a3532.ddf3a473795e5619d8d1e95820178389";

	private static final String ACCOUNTS = "/accounts";
    private static final String MESSAGES = "/messages";
    private static final String FOLDERS = "/folders/";
    private static final String SEPARATOR = "------";
    private String accountID;
    private String mailbox;

    final OAuth20Service service;
    final ZohoTokenManager tokenManager;

    static Zoho instance;
    
    private Zoho() {
    	service = ZohoServiceFactory.buildService(CLIENT_ID, CLIENT_SECRET);
        tokenManager = new ZohoTokenManager(service, REFRESH_TOKEN);
    }
 
    synchronized public static Zoho getInstance() {
    	if( instance == null ) {
            instance = new Zoho();
        }
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
                ZohoAccount account = data.get(0);
                accountID = account.accountId();
                mailbox = account.mailboxAddress();
        		return account;
        	}
        	else {
        		System.err.println( response.getCode() + "/" + response.getBody() );
        		return null;
        	}
        }
    }

    public void sendMessage(Message m) throws Exception {
        ZohoAccount account = getAccount();
         accountID = account.accountId();
         mailbox = account.mailboxAddress();

        var accessToken = new OAuth2AccessToken(tokenManager.getValidAccessToken());
        OAuthRequest request = new OAuthRequest(Verb.POST, MAIL_API_BASE + ACCOUNTS + "/" + accountID + MESSAGES);
        request.addHeader("Content-Type", "application/json; charset=utf-8");
        request.addHeader("Accept", "application/json");

        String fullContent = m.getContents() + SEPARATOR + JSON.encode(m);

        var body = JSON.encode(Map.of("fromAddress", mailbox,
                "toAddress", mailbox,
                "subject", m.getSubject(),
                "content", fullContent));

        request.setPayload(body);
        service.signRequest(accessToken, request);

        try (Response response = service.execute(request)) {
            if (!response.isSuccessful())
                throw new RuntimeException(response.getCode() + ": " + response.getBody());
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
            if (data == null || data.isEmpty()) return List.of();
            return data;
        }
    }

    public Message getMessage(String messageID) throws Exception {
        List<ZohoMessage> mails = getAllMessages();
        for (ZohoMessage message : mails) {
            Message m = Zoho.getInstance().getParsedMessage(accountID, message.folderId(), message.messageId()); //TODO check if getInstance is needed or not
            if (m.getId().equals(messageID)) {
                return m;
            }
        }
        return null;
    }

    public void deleteMessage(String messageId) throws Exception {
        List<ZohoMessage> mails = getAllMessages();
        Message m;
        String folderID = null;
        String zohoID = null;
        for (ZohoMessage message : mails) {
             m = Zoho.getInstance().getParsedMessage(accountID, message.folderId(), message.messageId());
            if (m.getId().equals(messageId)) {
                folderID = message.folderId();
                zohoID =  message.messageId();
                break;
            }
        }
        if (folderID == null) return;
        String accountID = getAccount().accountId();
        var accessToken = new OAuth2AccessToken(tokenManager.getValidAccessToken());
        OAuthRequest request = new OAuthRequest(Verb.DELETE, MAIL_API_BASE + ACCOUNTS + "/" + accountID + FOLDERS + folderID + MESSAGES + "/" + zohoID +"?expunge=true");
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

        content = content
                .replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?s)<[^>]*>", "")
                .replace("&quot;", "\"")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">");

        int separator = content.indexOf(SEPARATOR);
        if (separator < 0)
            return null;

        String metadata = content.substring(separator + SEPARATOR.length()).trim();

        return JSON.decode(metadata, Message.class);
    }

    /*To allow for testing this service automatically, using the Tester, it is necessary to start with a clean state, i.e.,
     *  with an empty mailbox. To achieve this, the Tester will pass as the first parameter of this Messages server the value true
     *  to indicate that the previous state should be ignored.*/
    public void deleteAllMessages() throws Exception {
        var msgs = getAllMessages();
        if (msgs != null)
            for (ZohoMessage msg : msgs){
                String accountID = getAccount().accountId();
                var accessToken = new OAuth2AccessToken(tokenManager.getValidAccessToken());
                OAuthRequest request = new OAuthRequest(Verb.DELETE, MAIL_API_BASE + ACCOUNTS + "/" + accountID + FOLDERS + msg.folderId() + MESSAGES + "/" + msg.messageId() +"?expunge=true");
                service.signRequest(accessToken, request);
                try (Response response = service.execute(request)) {
                    if (!response.isSuccessful())
                        throw new RuntimeException(response.getCode() + ": " + response.getBody());
                }
            }
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
        OAuthRequest request = new OAuthRequest(Verb.GET, MAIL_API_BASE + ACCOUNTS + "/" + accountId + FOLDERS + folderId + MESSAGES + "/" + messageId + "/content?includeBlockContent=true");
        request.addHeader("Accept", "application/json");
        service.signRequest(accessToken, request);

        try (Response response = service.execute(request)) {
            if (!response.isSuccessful())
                throw new RuntimeException(response.getCode() + ": " + response.getBody());
            var reply = JSON.decode(response.getBody(), ZohoContentReply.class);
            System.out.println("getMessageContent test " + reply + "\n");
            return reply.data().content();
        }
    }

    public List<String> getMessageQuery(String query) throws Exception {
        List<ZohoMessage> mails = getAllMessages();
        if (mails == null || mails.isEmpty()) return List.of();
        List<String> messages = new ArrayList<>();
        for (ZohoMessage mail : mails) {
            Message m = Zoho.getInstance().getParsedMessage(accountID,mail.folderId(),mail.messageId());
            if(query == null ||m.getSubject().toLowerCase().contains(query.toLowerCase()) || m.getContents().toLowerCase().contains(query.toLowerCase()))
                messages.add(m.getId());
        }
        return messages;
    }
}