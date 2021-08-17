package message;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
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

    public String getTags() {
        return this.getProperty("TAGS");
    }
    public String getProperty(final String name) {
        if (null == this.properties) {
            this.properties = new HashMap<String, String>();
        }

        return this.properties.get(name);
    }
}
