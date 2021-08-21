package message;

import common.MessageConst;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 该类是由生产者传递的message
 * 这个类没有queueId，说明数据给broker的时候，不关心写到那个queueId
 * 同时注意Message在写的时候，只会写到master broker，不回写到slave broker中
 * @author luo
 */
@Data
public class Message implements Serializable {

    private String topic;
    /**
     * 这个属性到写入mappedByteBuffer时，要序列化的
     * 该属性主要用来在commitLog中定位该消息
     */
    private Map<String, String> properties;
    private byte[] body;

    public Message(String topic, String tags, byte[] body) {
        this.topic = topic;
        this.body = body;
        if(tags !=null && tags.length()>0){
            setTags(tags);
        }
    }
    public void setTags(String tags){
        putProperty(MessageConst.propertyTags,tags);
    }

    public String getTags() {
        return this.getProperty(MessageConst.propertyTags);
    }
    public String getProperty(final String name) {
        if (null == this.properties) {
            this.properties = new HashMap<String, String>();
        }

        return this.properties.get(name);
    }

    void putProperty(final String name,final String value){
        if(null == properties){
            properties = new HashMap<>();
        }
        properties.put(name,value);
    }
}
