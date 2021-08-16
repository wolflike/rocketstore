package core;

import lombok.Data;

import java.util.Map;

/**
 * @author luo
 */

@Data
public class DispatchRequest {

    private final String topic;
    private final int queueId;
    private final long commitLogOffset;
    private int msgSize;
    private final long tagsCode;
    private final long storeTimestamp;
    private final long consumeQueueOffset;
    private final String keys;
    private final boolean success;
    private final String uniqKey;

    private final int sysFlag;
    private final long preparedTransactionOffset;
    private final Map<String, String> propertiesMap;
    private byte[] bitMap;

    private int bufferSize = -1;//the buffer size maybe larger than the msg size if the message is wrapped by something


    public DispatchRequest(String topic, int queueId, long commitLogOffset, int msgSize, long tagsCode, long storeTimestamp, long consumeQueueOffset, String keys, boolean success, String uniqKey, int sysFlag, long preparedTransactionOffset, Map<String, String> propertiesMap, byte[] bitMap) {
        this.topic = topic;
        this.queueId = queueId;
        this.commitLogOffset = commitLogOffset;
        this.msgSize = msgSize;
        this.tagsCode = tagsCode;
        this.storeTimestamp = storeTimestamp;
        this.consumeQueueOffset = consumeQueueOffset;
        this.keys = keys;
        this.success = success;
        this.uniqKey = uniqKey;
        this.sysFlag = sysFlag;
        this.preparedTransactionOffset = preparedTransactionOffset;
        this.propertiesMap = propertiesMap;
        this.bitMap = bitMap;
    }
}
