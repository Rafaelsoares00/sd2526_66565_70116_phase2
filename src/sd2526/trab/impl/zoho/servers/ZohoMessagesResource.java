package sd2526.trab.impl.zoho.servers;

import jakarta.inject.Singleton;
import sd2526.trab.api.Message;
import sd2526.trab.api.java.Messages;
import sd2526.trab.api.java.Result;
import sd2526.trab.api.rest.RestMessages;
import sd2526.trab.impl.api.java.AdminMessages;
import sd2526.trab.impl.java.servers.JavaMessagesZoho;
import sd2526.trab.impl.rest.servers.RestMessagesResource;
import sd2526.trab.impl.rest.servers.RestResource;

import java.util.List;

@Singleton
public class ZohoMessagesResource extends RestResource implements RestMessages, AdminMessages {

    protected Messages impl;

    public ZohoMessagesResource() {
        impl = new JavaMessagesZoho();
    }
    public ZohoMessagesResource(boolean gw) {

    }

    @Override
    public String postMessage(String pwd, Message msg) {
        return super.resultOrThrow( impl.postMessage(pwd, msg) );
    }

    @Override
    public Message getMessage(String name, String mid, String pwd) {
        return super.resultOrThrow( impl.getInboxMessage(name, mid, pwd) );
    }

    @Override
    public List<String> getMessages(String name, String pwd, String query) {
        if( query != null && ! query.isEmpty() )
            return super.resultOrThrow( impl.searchInbox( name, pwd, query) );
        else
            return super.resultOrThrow(impl.getAllInboxMessages(name, pwd));
    }

    @Override
    public void removeFromUserInbox(String name, String mid, String pwd) {
        super.resultOrThrow( impl.removeInboxMessage(name, mid, pwd) );

    }

    @Override
    public void deleteMessage(String name, String mid, String pwd) {
        super.resultOrThrow( impl.deleteMessage(name, mid, pwd));
    }

    @Override
    public Result<Void> remotePostMessage(Message m) {
        return null;
    }

    @Override
    public Result<Void> remoteDeleteMessage(String mid) {
        return null;
    }

    @Override
    public Result<Void> remoteDeleteUserInbox(String name) {
        return null;
    }
}