package message;

import lombok.Data;

/**
 * @author luo
 */
@Data
public class MessageExtBrokerInner extends MessageExt {
    private String propertiesString;
    private long tagsCode;

}
