package sd2526.trab.impl.zoho;

import sd2526.trab.api.Message;
import sd2526.trab.api.User;
import sd2526.trab.api.java.Messages;
import sd2526.trab.api.java.Result;
import sd2526.trab.impl.java.clients.Clients;
import sd2526.trab.impl.zoho.zohoAPI.msgs.ZohoMessage;

import java.util.List;

import static sd2526.trab.api.java.Result.ErrorCode.*;
import static sd2526.trab.api.java.Result.*;

public class ZohoMessages implements Messages {

    private final Zoho zoho = Zoho.getInstance();

    @Override
    public Result<String> postMessage(String pwd, Message msg) {
        //error handling

        try {

        } catch (Exception x) {
            x.printStackTrace();
            return error(INTERNAL_ERROR);
        }
        return null;
    }

    @Override
    public Result<Message> getInboxMessage(String name, String mid, String pwd) {
        //error handling

        try {
            ZohoMessage zohoMsg = zoho.getMessage(mid);
            if (zohoMsg == null)
                return error(NOT_FOUND);
            else {
                Message msg = zoho.getParsedMessage(zoho.getAccount().accountId(), zohoMsg.folderId(), zohoMsg.messageId());
                if (msg != null)
                    return ok(msg);
                else
                    return error(NOT_FOUND);
            }
        } catch (Exception x) {
            x.printStackTrace();
            return error(INTERNAL_ERROR);
        }
    }

    @Override
    public Result<List<String>> getAllInboxMessages(String name, String pwd) {
        //error handling

        try {
            var msgs = zoho.getAllMessages();
            if (msgs == null)
                return ok(List.of());
//            else
//                return ?
        } catch (Exception x) {
            x.printStackTrace();
            return error(INTERNAL_ERROR);
        }
        return null;
    }

    @Override
    public Result<Void> removeInboxMessage(String name, String mid, String pwd) {
        //error handling

        try {
            ZohoMessage zohoMsg = zoho.getMessage(mid);
            if (zohoMsg == null)
                return error(NOT_FOUND);
            else {
                zoho.deleteMessage(zohoMsg.messageId(), zohoMsg.folderId());
                return ok();
            }
        } catch (Exception x) {
            x.printStackTrace();
            return error(INTERNAL_ERROR);
        }
    }

    @Override
    public Result<Void> deleteMessage(String name, String mid, String pwd) {
        //error handling

        try {
            ZohoMessage zohoMsg = zoho.getMessage(mid);
            if (zohoMsg == null)
                return error(NOT_FOUND);
            else {
                //only delete if posted less than 30 seconds ago
                zoho.deleteMessage(zohoMsg.messageId(), zohoMsg.folderId());
                return ok();
            }
        } catch (Exception x) {
            x.printStackTrace();
            return error(INTERNAL_ERROR);
        }
    }

    @Override
    public Result<List<String>> searchInbox(String name, String pwd, String query) {
        //error handling

        try {
            //filter messages by query
        } catch (Exception x) {
            x.printStackTrace();
            return error(INTERNAL_ERROR);
        }
        return null;
    }

    protected Result<User> getUser(String user, String pwd) {
        try {
            var name = user.split("@", 2)[0];
            return Clients.UsersClient.get().getUser( name, pwd);
        } catch (Exception x) {
            x.printStackTrace();
            return error(INTERNAL_ERROR);
        }
    }
}
