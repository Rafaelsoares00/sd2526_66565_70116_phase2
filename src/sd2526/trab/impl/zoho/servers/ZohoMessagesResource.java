package sd2526.trab.impl.zoho.servers;

import jakarta.inject.Singleton;
import sd2526.trab.impl.rest.servers.RestMessagesResource;
import sd2526.trab.impl.zoho.ZohoMessages;

@Singleton
public class ZohoMessagesResource extends RestMessagesResource {

    public ZohoMessagesResource() {
        impl = new ZohoMessages();
    }
}