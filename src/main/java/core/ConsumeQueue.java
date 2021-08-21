package core;

import store.MappedFile;
import store.MappedFileQueue;

import java.io.File;
import java.nio.ByteBuffer;

/**
 * @author luo
 */
public class ConsumeQueue {

    /**
     * ConsumeQueue存储一个单元的基本大小（20字节）
     */
    public static final int CQ_STORE_UNIT_SIZE = 20;

    private final DefaultMessageStore defaultMessageStore;
    private final String topic;
    private final int queueId;
    private final String storePath;
    private final int mappedFileSize;
    private final MappedFileQueue mappedFileQueue;

    public ConsumeQueue(DefaultMessageStore defaultMessageStore,
                        String topic, int queueId, String storePath, int mappedFileSize) {
        this.defaultMessageStore = defaultMessageStore;
        this.topic = topic;
        this.queueId = queueId;
        this.storePath = storePath;
        this.mappedFileSize = mappedFileSize;

        String queueDir = this.storePath
                + File.separator + topic
                + File.separator + queueId;

        this.mappedFileQueue = new MappedFileQueue(queueDir, mappedFileSize, defaultMessageStore.getAllocateMappedFileService());
    }

    /**
     * 将commitLog的offset记录在consumeQueue中
     * @param cqOffset 在这个offset写数据
     * @param commitLogOffset 8字节
     * @param size 4字节
     * @param tagsCode hashCode 8字节
     * @return
     */
    private boolean putMessagePositionInfo(final long cqOffset,final long commitLogOffset,
                                           final int size, final long tagsCode) {

        return true;
    }


    public long getLastOffset(){

        long lastOffset = -1;

        int logicFileSize = this.mappedFileSize;

        MappedFile mappedFile = this.mappedFileQueue.getLastMappedFile();
        if (mappedFile != null) {

            int position = mappedFile.getWrotePosition() - CQ_STORE_UNIT_SIZE;
            if (position < 0){
                position = 0;
            }

            //创建一个新的字节缓冲区
            ByteBuffer byteBuffer = mappedFile.sliceByteBuffer();
            //设置position
            byteBuffer.position(position);
            for (int i = 0; i < logicFileSize; i += CQ_STORE_UNIT_SIZE) {
                long offset = byteBuffer.getLong();
                int size = byteBuffer.getInt();
                byteBuffer.getLong();

                if (offset >= 0 && size > 0) {
                    lastOffset = offset + size;
                } else {
                    break;
                }
            }
        }

        return lastOffset;
    }

    public String getTopic() {
        return topic;
    }

    public int getQueueId() {
        return queueId;
    }

    public void putMessagePositionInfoWrapper(DispatchRequest request){
        //调用putMessagePositionInfo

    }
}
