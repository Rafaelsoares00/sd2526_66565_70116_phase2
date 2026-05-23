package sd2526.trab.impl.rest.servers;

import sd2526.trab.api.Message;
import sd2526.trab.api.rest.RestMessages;
import sd2526.trab.impl.api.rest.RestAdminMessages;
import sd2526.trab.impl.java.servers.JavaMessages;
import sd2526.trab.impl.kafka.*;

import java.util.List;
import java.util.logging.Logger;


public class ReplicatedRestMessagesResource extends RestResource implements RestMessages, RestAdminMessages {
    private static Logger log = Logger.getLogger(ReplicatedRestMessagesResource.class.getName());
    private static final JavaMessages impl = JavaMessages.getInstance();
    private ReplicationManager replicationManager;

    public ReplicatedRestMessagesResource() {
        this.replicationManager = new ReplicationManager();
    }

    @Override
    public String postMessage(String pwd, Message msg) {
        return replicationManager.submit(new Operations("POST", pwd, msg, null, null, null, -1),String.class);
    }

    @Override
    public Message getMessage(String name, String mid, String pwd) {
        replicationManager.syncRead();
        return impl.getInboxMessage(name, mid, pwd).value();
    }

    @Override
    public List<String> getMessages(String name, String pwd, String query) {
        replicationManager.syncRead();
        if (query == null || query.isEmpty()) return impl.getAllInboxMessages(name, pwd).value();
        else return impl.searchInbox(name, pwd, query).value();
    }

    @Override
    public void removeFromUserInbox(String name, String mid, String pwd) {
            replicationManager.submit(new Operations("REMOVE", pwd, null, name, mid, null, -1), Void.class);
    }

    @Override
    public void deleteMessage(String name, String mid, String pwd) {
        replicationManager.submit(new Operations("DELETE", pwd, null, name, mid, null, -1), Void.class);
    }

    @Override
    public void remotePostMessage(Message m) {
        String senderDomain = m.senderAddress().split("@")[1];
        long domainVersion = ReplicationManager.currentVersion;
        replicationManager.submit(new Operations("REMOTE_POST", null, m, null, null, senderDomain, domainVersion), Void.class);
    }

    @Override
    public void remoteDeleteMessage(String mid) {
        replicationManager.submit(new Operations("REMOTE_DELETE", null, null, null, mid, null, -1), Void.class);
    }

    @Override
    public void remoteDeleteUserInbox(String name) {
        replicationManager.submit(new Operations("REMOTE_DELETE_INBOX", null, null, name, null, null, -1), Void.class);
    }
}
