package sd2526.trab.impl.zoho.zohoAPI.msgs;

public record ZohoMessage(
        String summary,
        String sentDateInGMT,
        int calendarType,
        String subject,
        String messageId,
        String threadCount,
        String flagId,
        String status2,
        String priority,
        String hasInline,
        String toAddress,
        String folderId,
        String ccAddress,
        String threadId,
        String hasAttachment,
        String size,
        String sender,
        String receivedTime,
        String fromAddress,
        String status
) {}
