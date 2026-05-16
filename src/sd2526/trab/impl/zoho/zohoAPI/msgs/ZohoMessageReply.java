package sd2526.trab.impl.zoho.zohoAPI.msgs;

import java.util.List;

public record ZohoMessageReply(ZohoStatus status, List<ZohoMessage> data) {
}
