package store;

import core.AppendMessageCallback;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import message.MessageExtBrokerInner;
import result.AppendMessageResult;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author luo
 */
@Data
@Slf4j
public class MappedFile {

    private String fileName;
    private long fileFromOffset;
    private File file;
    private MappedByteBuffer mappedByteBuffer;
    protected ByteBuffer writeBuffer = null;
    protected int fileSize;
    protected FileChannel fileChannel;
    protected final AtomicInteger wrotePosition = new AtomicInteger(0);


    public int getWrotePosition(){
        return 0;
    }

    public ByteBuffer sliceByteBuffer(){
        return mappedByteBuffer.slice();
    }

    /**
     * 这个方法的本质都是把数据写到byteBuffer中
     * @param msg
     * @param callback
     * @return
     */
    public AppendMessageResult appendMessage(MessageExtBrokerInner msg, AppendMessageCallback callback){
        int currentPos = this.wrotePosition.get();
        ByteBuffer byteBuffer = writeBuffer != null ? writeBuffer.slice() : this.mappedByteBuffer.slice();
        byteBuffer.position(currentPos);
        AppendMessageResult result = callback.doAppend(this.getFileFromOffset(), byteBuffer, this.fileSize - currentPos, msg);
        this.wrotePosition.addAndGet(result.getWroteBytes());
        return result;
    }

    /**
     * 这个api存粹是直接让应用写入数据的
     * 可以看到数据是写进fileChannel中的
     * @param data
     * @return
     */
    public boolean appendMessage(final byte[] data) {
        int currentPos = this.wrotePosition.get();

        if ((currentPos + data.length) <= this.fileSize) {
            try {
                this.fileChannel.position(currentPos);
                this.fileChannel.write(ByteBuffer.wrap(data));
            } catch (Throwable e) {

            }
            this.wrotePosition.addAndGet(data.length);
            return true;
        }

        return false;
    }
    /**
     * @return The current flushed position
     */
    public int flush(final int flushLeastPages) {
        return 0;
//        if (this.isAbleToFlush(flushLeastPages)) {
//            if (this.hold()) {
//                int value = getReadPosition();
//
//                try {
//                    //We only append data to fileChannel or mappedByteBuffer, never both.
//                    if (writeBuffer != null || this.fileChannel.position() != 0) {
//                        this.fileChannel.force(false);
//                    } else {
//                        this.mappedByteBuffer.force();
//                    }
//                } catch (Throwable e) {
//                    log.error("Error occurred when force data to disk.", e);
//                }
//
//                this.flushedPosition.set(value);
//                this.release();
//            } else {
//                log.warn("in flush, hold failed, flush offset = " + this.flushedPosition.get());
//                this.flushedPosition.set(getReadPosition());
//            }
//        }
//        return this.getFlushedPosition();

    }

}
