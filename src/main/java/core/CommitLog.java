package core;

import message.Message;
import message.MessageExt;
import message.MessageExtBatch;
import message.MessageExtBrokerInner;
import result.*;
import store.MappedFile;
import store.MappedFileQueue;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author luo
 * 数据是写到mappedFileQueue中的
 * 多线程共用这一个类，所以肯定是需要锁机制的
 */
public class CommitLog {
    /**
     * Message's MAGIC CODE daa320a7
     */
    public final static int MESSAGE_MAGIC_CODE = 0xAAAABBBB;

    /**
     * End of file empty MAGIC CODE cbd43194
     */
    protected final static int BLANK_MAGIC_CODE = 0xEEEEFFFF;

    /**
     * <topic-queueId,offset>
     */
    protected HashMap<String, Long> topicQueueTable = new HashMap<String, Long>(1024);
    protected final MappedFileQueue mappedFileQueue;
    protected final DefaultMessageStore defaultMessageStore;
    private final ReentrantLock putMessageLock = new ReentrantLock();
    private final FlushCommitLogService flushCommitLogService;

    private final AppendMessageCallback appendMessageCallback;

    public CommitLog(DefaultMessageStore defaultMessageStore) {
        this.mappedFileQueue = null;
        this.defaultMessageStore = defaultMessageStore;
        this.appendMessageCallback = new DefaultAppendMessageCallback(MessageStoreConfig.messageSize);
        if (FlushDiskType.SYNC_FLUSH == MessageStoreConfig.getFlushDiskType()) {
            this.flushCommitLogService = new GroupCommitService();
        } else {
            this.flushCommitLogService = new FlushRealTimeService();
        }
    }

    public void start(){
        this.flushCommitLogService.start();
    }

    public void shutdown(){
        this.flushCommitLogService.shutdown();
    }
    public long flush(){
        return 0L;
    }
    public boolean load(){
        return true;
    }


    /**
     * 这里是保存message的核心逻辑
     * @param msg
     * @return
     */
    public PutMessageResult putMessage(final MessageExtBrokerInner msg){
        //这里会有多线程执行putMessage
        //这里就是多个线程根据topic-queueId的维度进行写入数据

        MappedFile mappedFile = this.mappedFileQueue.getLastMappedFile();
        AppendMessageResult result = null;
        putMessageLock.lock();
        try {

            result = mappedFile.appendMessage(msg, appendMessageCallback);
        }finally {
            putMessageLock.unlock();
        }

        //构建putMessageResult
        PutMessageResult putMessageResult = new PutMessageResult(PutMessageStatus.PUT_OK, result);

        //刷新数据到磁盘
        handleDiskFlush(result,putMessageResult,msg);

        return putMessageResult;
    }
    public void handleDiskFlush(AppendMessageResult result, PutMessageResult putMessageResult, MessageExt messageExt){

        //根据业务场景需要，选择异步刷新磁盘还是同步刷新

        //同步
        if(FlushDiskType.SYNC_FLUSH == MessageStoreConfig.getFlushDiskType()){
            final GroupCommitService service = (GroupCommitService) this.flushCommitLogService;
            GroupCommitRequest request = new GroupCommitRequest(result.getWroteOffset() + result.getWroteBytes());
            service.putRequest(request);

        }else{//异步
            //服务在处理刷盘之前是被阻塞的
            //直到被唤醒才能工作
            flushCommitLogService.wakeup();
        }

    }


    public PutMessageResult putMessages(List<Message> msg){
        return null;
    }

    public SelectMappedBufferResult getData(final long offset){
        return null;
    }

    public DispatchRequest makeDispatcherRequest(ByteBuffer byteBuffer){

        int bodyCRC = byteBuffer.getInt();

        int queueId = byteBuffer.getInt();

        int flag = byteBuffer.getInt();

        long queueOffset = byteBuffer.getLong();

        long physicOffset = byteBuffer.getLong();

        int sysFlag = byteBuffer.getInt();

        long bornTimeStamp = byteBuffer.getLong();

        return null;

//        return new core.DispatchRequest(
//                topic,
//                queueId,
//                physicOffset,
//                totalSize,
//                tagsCode,
//                storeTimestamp,
//                queueOffset,
//                keys,
//                uniqKey,
//                sysFlag,
//                preparedTransactionOffset,
//                propertiesMap
//        );
    }


    /**
     * 刷盘服务不管是异步还是同步，最终是要把数据刷新到mappedFileQueue
     * 调用其flush方法-mappedFile.flush->mappedBuffer.force
     */
    abstract class FlushCommitLogService extends ServiceThread {
        protected static final int RETRY_TIMES_OVER = 10;
    }

    class CommitRealTimeService extends FlushCommitLogService{

        @Override
        public String getServiceName() {
            return CommitRealTimeService.class.getSimpleName();
        }

        @Override
        public void run() {

        }
    }

    /**
     * 同步刷新
     */
    class FlushRealTimeService extends FlushCommitLogService{

        @Override
        public String getServiceName() {
            return FlushRealTimeService.class.getSimpleName();
        }

        @Override
        public void run() {

        }
    }

    /**
     * 异步刷新，一组数据一起刷入磁盘
     */
    class GroupCommitService extends FlushCommitLogService{

        private volatile List<GroupCommitRequest> requestsWrite = new ArrayList<GroupCommitRequest>();
        private volatile List<GroupCommitRequest> requestsRead = new ArrayList<GroupCommitRequest>();

        public synchronized void putRequest(final GroupCommitRequest request) {
            synchronized (this.requestsWrite) {
                this.requestsWrite.add(request);
            }
            this.wakeup();
        }

        @Override
        public String getServiceName() {
            return GroupCommitService.class.getSimpleName();
        }

        private void doCommit(){

        }

        @Override
        public void run() {

        }
    }
    public static class GroupCommitRequest {
        private final long nextOffset;

        public GroupCommitRequest(long nextOffset) {
            this.nextOffset = nextOffset;
        }
    }

    class DefaultAppendMessageCallback implements AppendMessageCallback {

        /**
         * File at the end of the minimum fixed length empty
         */
        private static final int END_FILE_MIN_BLANK_LENGTH = 4 + 4;

        private final ByteBuffer msgStoreItemMemory;

        private final StringBuilder keyBuilder = new StringBuilder();

        DefaultAppendMessageCallback(final int size) {
            this.msgStoreItemMemory = ByteBuffer.allocate(size+END_FILE_MIN_BLANK_LENGTH);
        }

        @Override
        public AppendMessageResult doAppend(long fileFromOffset, ByteBuffer byteBuffer, int maxBlank, MessageExtBrokerInner msg) {

            /**
             * commitLog中数据的物理偏移量
             */
            long wroteOffset = fileFromOffset + byteBuffer.position();

            byte[] topicData = msg.getTopic().getBytes(StandardCharsets.UTF_8);
            int topicLength = topicData.length;
            byte[] propertiesData = msg.getPropertiesString() == null ? null :
                    msg.getPropertiesString().getBytes(StandardCharsets.UTF_8);
            int propertiesLength =propertiesData == null ? 0 : propertiesData.length;
            int bodyLength =msg.getBody() == null ? 0 : msg.getBody().length;

            int msgLength = calMsgLength(topicLength,bodyLength,propertiesLength);

            keyBuilder.setLength(0);
            keyBuilder.append(msg.getTopic());
            keyBuilder.append('-');
            keyBuilder.append(msg.getQueueId());
            String key = keyBuilder.toString();
            Long queueOffset = CommitLog.this.topicQueueTable.get(key);
            if (null == queueOffset) {
                queueOffset = 0L;
                CommitLog.this.topicQueueTable.put(key, queueOffset);
            }

            //topicSize
            msgStoreItemMemory.putInt(msgLength);
            //magicCode
            msgStoreItemMemory.putInt(MESSAGE_MAGIC_CODE);
            //queueId
            msgStoreItemMemory.putInt(msg.getQueueId());
            //queueOffset
            msgStoreItemMemory.putLong(queueOffset);
            //physicalOffset
            msgStoreItemMemory.putLong(msg.getCommitLogOffset());
            //reConsumeTimes
            msgStoreItemMemory.putInt(msg.getReconsumeTimes());
            //body
            msgStoreItemMemory.putInt(bodyLength);
            msgStoreItemMemory.put(msg.getBody());
            //topic
            msgStoreItemMemory.put((byte) topicLength);
            msgStoreItemMemory.put(topicData);
            //properties
            msgStoreItemMemory.putShort((short) propertiesLength);
            msgStoreItemMemory.put(propertiesData);

            final long beginTimeMills = System.currentTimeMillis();

            // Write messages to the queue buffer
            byteBuffer.put(this.msgStoreItemMemory.array(), 0, msgLength);

            AppendMessageResult result = new AppendMessageResult(AppendMessageStatus.PUT_OK,wroteOffset,
                    msgLength,msg.getMsgId(),queueOffset,System.currentTimeMillis()- beginTimeMills);

            return result;
        }

        @Override
        public AppendMessageResult doAppend(long fileFromOffset, ByteBuffer byteBuffer, int maxBlank, MessageExtBatch messageExtBatch) {
            return null;
        }

        public int calMsgLength(int topicLength, int bodyLength, int propertiesLength){
            int fixed = 4 + 4 + 4 + 8 + 8 + 4 + 4 + 1 + 2;
            return fixed + topicLength + bodyLength + propertiesLength;
        }
    }
}
