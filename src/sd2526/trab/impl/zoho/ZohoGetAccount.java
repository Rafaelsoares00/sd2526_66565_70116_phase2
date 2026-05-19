package sd2526.trab.impl.zoho;

public class ZohoGetAccount {

    public static void main(String[] args) throws Exception {

        var account = Zoho.getInstance().getAccount();
        if (account != null) {
            System.out.printf("Account ID: %s, displayName: %s\n", account.accountId(), account.displayName());
//            String result = Zoho.getInstance().sendMessage(
//                    "msg 1",
//                    "rap.soares@zohomail.eu",
//                    "rap.soares@zohomail.eu",
//                    System.currentTimeMillis(),
//                    "Hello, this is a test message!");
            //System.out.println("sendMessage result: " + result); sendMessage, working 95% as intended
            //String result = Zoho.getInstance().getAllMessages().toString();
            //String result = Zoho.getInstance().getMessage("Your").toString();
            //System.out.printf(result);

//            var messages = Zoho.getInstance().getAllMessages();
//            for (var msg : messages) {
//                System.out.println("messageId: " + msg.messageId() + ", folderId: " + msg.folderId() + ", subject: " + msg.subject());
//            }
//
//            var first = messages.get(0);
//            Zoho.getInstance().deleteMessage(first.messageId(), first.folderId());
//            System.out.println("Deleted message: " + first.messageId());
        }
        else
            System.err.println("Error...");
    }
}
