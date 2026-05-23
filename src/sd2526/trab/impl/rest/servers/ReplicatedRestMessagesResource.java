package sd2526.trab.impl.rest.servers;

import com.google.gson.Gson;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import org.hsqldb.persist.Log;
import sd2526.trab.api.Message;
import sd2526.trab.api.java.Result;
import sd2526.trab.api.rest.RestMessages;
import sd2526.trab.impl.api.java.AdminMessages;
import sd2526.trab.impl.api.rest.RestAdminMessages;
import sd2526.trab.impl.java.servers.JavaMessages;
import sd2526.trab.impl.kafka.*;
import sd2526.trab.impl.utils.IP;
import sd2526.trab.impl.utils.JSON;
import sd2526.trab.impl.utils.SyncPoint;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;


public class ReplicatedRestMessagesResource implements RestMessages, RestAdminMessages {
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
        replicationManager.submit(new Operations("REMOTE_POST", null, m, null, null, null, -1), Void.class);
    }

    @Override
    public void remoteDeleteMessage(String mid) {

    }

    @Override
    public void remoteDeleteUserInbox(String name) {

    }
}
