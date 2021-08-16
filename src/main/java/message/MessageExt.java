package message;

import lombok.Data;

import java.net.SocketAddress;

/**
 * @author luo
 */
@Data
public class MessageExt extends Message{
    private String brokerName;
    private String msgId;
    /**
     *队列id
     */
    private int queueId;

    /**
     * 存储大小
     */
    private int storeSize;

    /**
     * 队列逻辑offset
     */
    private long queueOffset;
    /**
     * 文件中的偏移量
     */
    private long commitLogOffset;
    private int reconsumeTimes;
}
