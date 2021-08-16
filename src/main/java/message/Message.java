package message;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * @author luo
 */
@Data
public class Message implements Serializable {

    private String topic;
    /**
     * 这个属性到写入mappedByteBuffer时，要序列化的
     */
    private Map<String, String> properties;
    private byte[] body;
}
