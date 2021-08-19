package store;

import core.AppendMessageCallback;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import message.MessageExtBrokerInner;
import result.AppendMessageResult;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 一个MappedFile管理一个文件
 * 文件的命名规则：以文件大小命名，长度统一为20
 * 比如以1G大小为一个文件：
 * 00000000000000000000，代表第二个文件
 * 00000000001073741824，代表第二个文件
 * @author luo
 *
 *
 *
 * 这里描述一下wrotePostion、commitedPosition、flushedPosition三者之间的关系
 *
 * directByteBuffer             |||||||||commitedPosition|||||||||||
 * mappedByteBuffer             |||||||||                ||               ||wrotePosition|||||||
 * physicalFile                 |||||||||                ||flushedPosition|||||||||
 */
@Setter
@Getter
@Slf4j
public class MappedFile {

    public static final int OS_PAGE_SIZE = 1024 * 4;

    /**
     * 文件名
     */
    private String fileName;
    /**
     * 文件的偏移量，文件首地址
     */
    private long fileFromOffset;
    /**
     * 管理的文件对象
     */
    private File file;
    /**
     * 读写文件的工具
     */
    private MappedByteBuffer mappedByteBuffer;

    /**
     * 也是读写文件的工具
     */
    protected FileChannel fileChannel;

    /**
     * 堆外内存
     */
    protected ByteBuffer writeBuffer = null;

    private final AtomicInteger flushedPosition = new AtomicInteger(0);

    /**
     * 用于transientStorePool
     */
    protected final AtomicInteger committedPosition = new AtomicInteger(0);
    /**
     * 已写的位置
     */
    protected final AtomicInteger wrotePosition = new AtomicInteger(0);

    /**
     * 文件大小
     */
    protected int fileSize;


    /**
     * 存储的时间戳
     */
    private volatile long storeTimestamp = 0;

    public MappedFile(final String fileName, final int fileSize) {
        init(fileName, fileSize);
    }

    public int getFlushedPosition(){
        return flushedPosition.get();
    }


    public int getWrotePosition(){
        return 0;
    }

    public ByteBuffer sliceByteBuffer(){
        return mappedByteBuffer.slice();
    }

    private void init(final String fileName, final int fileSize){
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.file = new File(fileName);
        this.fileFromOffset = Long.parseLong(this.file.getName());
    }

    /**
     * 这个方法的本质都是把数据写到byteBuffer中
     * @param msg
     * @param callback
     * @return
     */
    public AppendMessageResult appendMessage(MessageExtBrokerInner msg, AppendMessageCallback callback){
        //写入ByteBuffer的当前位置
        int currentPos = this.wrotePosition.get();
        //有堆外内存，先放堆外内存，没有再用内存映射
        ByteBuffer byteBuffer = writeBuffer != null ? writeBuffer.slice() : this.mappedByteBuffer.slice();
        //从currentPos开始写
        byteBuffer.position(currentPos);
        //写的逻辑操作交给AppendMessageCallback
        AppendMessageResult result = callback.doAppend(this.getFileFromOffset(), byteBuffer, this.fileSize - currentPos, msg);
        //更新当前位置，加上已写入的字节数
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
        if (this.isAbleToFlush(flushLeastPages)) {
            if (this.hold()) {
                int value = getReadPosition();

                //直接把数据从flushedPosition刷到readPosition
                try {
                    //We only append data to fileChannel or mappedByteBuffer, never both.
                    if (writeBuffer != null || this.fileChannel.position() != 0) {
                        this.fileChannel.force(false);
                    } else {
                        this.mappedByteBuffer.force();
                    }
                } catch (Throwable e) {
                    log.error("Error occurred when force data to disk.", e);
                }

                this.flushedPosition.set(value);
                this.release();
            } else {
                log.warn("in flush, hold failed, flush offset = " + this.flushedPosition.get());
                this.flushedPosition.set(getReadPosition());
            }
        }
        return this.getFlushedPosition();

    }

    public synchronized boolean hold(){
        return true;
    }

    public void release(){

    }

    /**
     * 判断是否可以刷新
     * @param flushLeastPages 如果为0，那就是只要有数据就可以刷，如果大于0，那就是限定的页数
     * @return
     */
    private boolean isAbleToFlush(final int flushLeastPages) {
        int flush = this.flushedPosition.get();
        int write = getReadPosition();

        if (this.isFull()) {
            return true;
        }

        if (flushLeastPages > 0) {
            return ((write / OS_PAGE_SIZE) - (flush / OS_PAGE_SIZE)) >= flushLeastPages;
        }

        return write > flush;
    }

    public int getReadPosition(){
        return 0;
    }
    public boolean isFull(){
        return true;
    }

}
