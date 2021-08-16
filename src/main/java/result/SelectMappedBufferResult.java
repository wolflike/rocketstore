package result;

import lombok.Data;
import store.MappedFile;

import java.nio.ByteBuffer;

/**
 * @author luo
 */
@Data
public class SelectMappedBufferResult {

    private final long startOffset;

    private final ByteBuffer byteBuffer;

    private int size;

    private MappedFile mappedFile;


    public SelectMappedBufferResult(long startOffset, ByteBuffer byteBuffer) {
        this.startOffset = startOffset;
        this.byteBuffer = byteBuffer;
    }
}
