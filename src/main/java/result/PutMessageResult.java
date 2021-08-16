package result;

import lombok.Data;

/**
 * @author luo
 */
@Data
public class PutMessageResult {
    private PutMessageStatus status;

    private AppendMessageResult appendMessageResult;

    public PutMessageResult(PutMessageStatus status, AppendMessageResult appendMessageResult) {
        this.status = status;
        this.appendMessageResult = appendMessageResult;
    }
}
