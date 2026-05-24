package sd2526.trab.impl.kafka;

import sd2526.trab.api.Message;
import sd2526.trab.api.User;

public record Operations(String type,
                         User user,
                         String pwd,
                         Message msg,
                         String name,
                         String mid,
                         String domain,
                         long domainVersion) {
}
