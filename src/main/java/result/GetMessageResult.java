package result;

import lombok.Data;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author luo
 */
@Data
public class GetMessageResult {
    private final List<SelectMappedBufferResult> messageMappedList =
            new ArrayList<SelectMappedBufferResult>(100);

    private final List<ByteBuffer> messageBufferList = new ArrayList<ByteBuffer>(100);

    private GetMessageStatus status;
    private long nextBeginOffset;
    private long minOffset;
    private long maxOffset;

    private int bufferTotalSize = 0;

    private boolean suggestPullingFromSlave = false;

    private int msgCount4Commercial = 0;
}
