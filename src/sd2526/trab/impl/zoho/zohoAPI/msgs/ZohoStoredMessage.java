package sd2526.trab.impl.zoho.zohoAPI.msgs;

public record ZohoStoredMessage(
        String zohoMailId,
        String id,
        String sender,
        String destination,
        long   creationTime,
        String content
) {}
