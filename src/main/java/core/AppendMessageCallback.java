package core;

import message.MessageExtBatch;
import message.MessageExtBrokerInner;
import result.AppendMessageResult;

import java.nio.ByteBuffer;

/**
 * @author luo
 * Write messages callback interface
 */
public interface AppendMessageCallback {
    /**
     * 在message序列化后，写入到MappedByteBuffer
     * @param fileFromOffset
     * @param byteBuffer 这是一个传入传出参数，最终的数据是要放在这个byteBuffer中的
     * @param maxBlank
     * @param msg
     * @return How many bytes to write
     */
    AppendMessageResult doAppend(final long fileFromOffset, final ByteBuffer byteBuffer,
                                 final int maxBlank, final MessageExtBrokerInner msg);

    /**
     * 在批量message序列化后，写入到MappedByteBuffer
     * @param fileFromOffset
     * @param byteBuffer
     * @param maxBlank
     * @param messageExtBatch
     * @return
     */
    AppendMessageResult doAppend(final long fileFromOffset, final ByteBuffer byteBuffer,
                                 final int maxBlank, final MessageExtBatch messageExtBatch);
}
