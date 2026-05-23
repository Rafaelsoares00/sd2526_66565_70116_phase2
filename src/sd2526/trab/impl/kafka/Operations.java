package sd2526.trab.impl.kafka;

import sd2526.trab.api.Message;

public record Operations(String type,
                         String pwd,
                         Message msg,
                         String name,
                         String mid,
                         String domain,
                         long domainVersion) {
}
