package result;

import lombok.Data;

/**
 * @author luo
 */
@Data
public class PutMessageResult {
    private PutMessageStatus putMessageStatus;

    private AppendMessageResult appendMessageResult;

    public PutMessageResult(PutMessageStatus status, AppendMessageResult appendMessageResult) {
        this.putMessageStatus = status;
        this.appendMessageResult = appendMessageResult;
    }

}
