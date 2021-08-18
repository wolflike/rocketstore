package core;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author luo
 * 数据是写到mappedFileQueue中的
 * 多线程共用这一个类，所以肯定是需要锁机制的
 */
@Slf4j
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
        //刷新的思路是把mappedFile中的byteBuffer数据flush到磁盘去
        //同步
        if(FlushDiskType.SYNC_FLUSH == MessageStoreConfig.getFlushDiskType()){

            //总结来看，同步刷新本质是提交要刷新的位置先扔给队列（list）
            //线程不断从list取位置，然后给给mappedFileQueue去刷新位置
            //等处理完了，把状态给flushOKFuture
            //我们在当前层阻塞等待flushOKFuture的结果
            //所以可以看到同步刷新用了异步类解决这种异步问题
            //应该吸收学习

            final GroupCommitService service = (GroupCommitService) this.flushCommitLogService;
            //将刷新的位置通知mappedFile
            GroupCommitRequest request = new GroupCommitRequest(result.getWroteOffset() + result.getWroteBytes());
            //把请求放到list中，service线程会不断从list中拿request做刷新
            //所以这里是一个异步操作（因为你把数据放到list后，你不知道什么时候能拿到你想要的结果）
            service.putRequest(request);
            //因为上面的异步操作，所以想获取上面操作返回的参数就必须用CompletableFuture，异步获取数据
            CompletableFuture<PutMessageStatus> flushOKFuture = request.future();
            PutMessageStatus flushStatus = null;
            try {
                //允许5秒的超时
                flushStatus = flushOKFuture.get(MessageStoreConfig.syncFlushTimeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                e.printStackTrace();
            }
            if (flushStatus != PutMessageStatus.PUT_OK) {
                log.error("do group commit, wait for flush failed, topic: " + messageExt.getTopic() + " tags: " + messageExt.getTags());
                putMessageResult.setPutMessageStatus(PutMessageStatus.FLUSH_DISK_TIMEOUT);
            }

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
            //这里就是唤醒了，当提交了request，说明有要刷盘的需求了，所以唤醒该线程
            //通过countDownLatch来控制
            this.wakeup();
        }

        private void swapRequests() {
            List<GroupCommitRequest> tmp = this.requestsWrite;
            this.requestsWrite = this.requestsRead;
            this.requestsRead = tmp;
        }

        @Override
        public String getServiceName() {
            return GroupCommitService.class.getSimpleName();
        }

        private void doCommit(){
            //这个核心就是刷新磁盘
            synchronized (this.requestsRead) {
                if (!this.requestsRead.isEmpty()) {
                    for (GroupCommitRequest req : this.requestsRead) {

                        //如果说flushedWhere都大于等于nextOffset，那就没必要刷盘了，
                        boolean flushOK = CommitLog.this.mappedFileQueue.getFlushedWhere() >= req.getNextOffset();
                        // There may be a message in the next file, so a maximum of
                        // two times the flush
                        for (int i = 0; i < 2 && !flushOK; i++) {
                            CommitLog.this.mappedFileQueue.flush(0);
                            flushOK = CommitLog.this.mappedFileQueue.getFlushedWhere() >= req.getNextOffset();
                        }


                        req.wakeupCustomer(flushOK ? PutMessageStatus.PUT_OK : PutMessageStatus.FLUSH_DISK_TIMEOUT);
                    }

                    long storeTimestamp = CommitLog.this.mappedFileQueue.getStoreTimestamp();
                    if (storeTimestamp > 0) {
                        //CommitLog.this.defaultMessageStore.getStoreCheckpoint().setPhysicMsgTimestamp(storeTimestamp);
                    }

                    this.requestsRead.clear();
                } else {
                    // Because of individual messages is set to not sync flush, it
                    // will come to this process
                    CommitLog.this.mappedFileQueue.flush(0);
                }
            }

        }

        @Override
        public void run() {
            CommitLog.log.info(this.getServiceName() + " service started");

            //只有线程是活着的，才可以继续
            while (!this.isStopped()) {
                try {
                    //这里就是阻塞等待呀，
                    //只有被唤醒才能继续执行
                    //很显然必须是其他线程主动去唤醒（wakeup），
                    //该线程才能被唤醒
                    this.waitForRunning(10);
                    this.doCommit();
                } catch (Exception e) {
                    CommitLog.log.warn(this.getServiceName() + " service has exception. ", e);
                }
            }

            // Under normal circumstances shutdown, wait for the arrival of the
            // request, and then flush
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                CommitLog.log.warn("GroupCommitService Exception, ", e);
            }

            synchronized (this) {
                this.swapRequests();
            }

            this.doCommit();

            CommitLog.log.info(this.getServiceName() + " service end");

        }
    }
    @Getter
    @Setter
    public static class GroupCommitRequest {
        /**
         * 将要刷新到的位置
         */
        private final long nextOffset;

        /**
         * 磁盘刷新完成后的状态，异步获取
         * 也就是说：flushOKFuture里面装的status是等某个任务执行完后，才放进去的
         * 你可以在其他地方执行get方法，但是获取不到里面的数据，直到任务完成后，才能获取到
         */
        private CompletableFuture<PutMessageStatus> flushOKFuture = new CompletableFuture<>();

        public GroupCommitRequest(long nextOffset) {
            this.nextOffset = nextOffset;
        }

        public void wakeupCustomer(final PutMessageStatus putMessageStatus) {
            this.flushOKFuture.complete(putMessageStatus);
        }
        public CompletableFuture<PutMessageStatus> future() {
            return flushOKFuture;
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

            //todo maxBlank是用来判断一个文件是否不够写入了

            /**
             * commitLog中数据的物理偏移量（文件偏移量+当前byteBuffer的position）
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
