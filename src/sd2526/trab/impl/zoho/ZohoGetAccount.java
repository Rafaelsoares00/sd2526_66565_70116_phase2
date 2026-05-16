package sd2526.trab.impl.zoho;

public class ZohoGetAccount {

    public static void main(String[] args) throws Exception {

        var account = Zoho.getInstance().getAccount();
        if (account != null) {
            //String result = Zoho.getInstance().getAllMessages().toString();
            String result = Zoho.getInstance().getMessage("Your").toString();
            System.out.printf("Account ID: %s, displayName: %s\n", account.accountId(), account.displayName());
            System.out.printf(result);
        }
        else
            System.err.println("Error...");
    }
}
