package result;

import lombok.Data;

/**
 * @author luo
 */
@Data
public class AppendMessageResult {

    private AppendMessageStatus status;

    /**
     * 将要去写的offset
     */
    private long wroteOffset;
    /**
     * 写入的字节数
     */
    private int wroteBytes;
    /**
     * Message ID
     */
    private String msgId;

    /**
     * Consume queue's offset(step by one)
     */
    private long logicsOffset;

    private long pageCacheRT = 0;

    private int msgNum = 1;

    public AppendMessageResult(AppendMessageStatus status, long wroteOffset, int wroteBytes, String msgId,
                               long logicsOffset, long pageCacheRT) {
        this.status = status;
        this.wroteOffset = wroteOffset;
        this.wroteBytes = wroteBytes;
        this.msgId = msgId;
        this.logicsOffset = logicsOffset;
        this.pageCacheRT = pageCacheRT;
    }
}
