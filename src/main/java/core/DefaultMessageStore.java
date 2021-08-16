package core;

import message.Message;
import message.MessageExtBrokerInner;
import result.GetMessageResult;
import result.PutMessageResult;
import result.QueryMessageResult;
import result.SelectMappedBufferResult;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author luo
 */
public class DefaultMessageStore implements MessageStore {

    /**
     * 保存ConsumeQueue
     * <topic,<queueId,consumeQueue>>
     */
    private final ConcurrentMap<String, ConcurrentMap<Integer, ConsumeQueue>> consumeQueueTable;

    private final CommitLog commitLog;

    /**
     * 该服务是轮询commitLog，获取physicalOffset写consumeQueue的
     */
    private final RePutMessageService reputMessageService;

    private final LinkedList<CommitLogDispatcher> dispatcherList;



    public DefaultMessageStore() {
        this.consumeQueueTable = new ConcurrentHashMap<>();
        this.commitLog = new CommitLog(this);
        this.reputMessageService = new RePutMessageService();
        dispatcherList = new LinkedList<>();
        //这里就不写索引了，直接写consumeQueue算了
        this.dispatcherList.addLast(new CommitLogDispatcherBuildConsumeQueue());
    }

    @Override
    public boolean load() {
        return false;
    }

    @Override
    public void start(){
        reputMessageService.start();

    }

    @Override
    public void shutdown() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public PutMessageResult putMessage(MessageExtBrokerInner msg) {
        return commitLog.putMessage(msg);
    }

    @Override
    public PutMessageResult putMessages(List<Message> messages) {
        return commitLog.putMessages(messages);
    }

    @Override
    public GetMessageResult getMessage(String group, String topic, int queueId, long offset, int maxMsgNums) {
        return null;
    }

    @Override
    public Message lookMessageByOffset(long commitLogOffset) {
        return null;
    }

    @Override
    public SelectMappedBufferResult selectOneMessageByOffset(long commitLogOffset) {
        return null;
    }

    @Override
    public SelectMappedBufferResult selectOneMessageByOffset(long commitLogOffset, int msgSize) {
        return null;
    }

    @Override
    public SelectMappedBufferResult getCommitLogData(long offset) {
        return null;
    }

    @Override
    public QueryMessageResult queryMessage(String topic, String key, int maxNum, long begin, long end) {
        return null;
    }

    @Override
    public ConsumeQueue getConsumeQueue(String topic, int queueId) {
        ConcurrentMap<Integer, ConsumeQueue> map = consumeQueueTable.get(topic);
        if (map == null) {
            return null;
        }
        return map.get(queueId);
    }

    /**
     * 当queue还没有初始化时，需要帮忙初始化一下
     * @param topic
     * @param queueId
     * @return
     */
    public ConsumeQueue findConsumeQueue(String topic, int queueId){
        ConcurrentMap<Integer, ConsumeQueue> map = consumeQueueTable.get(topic);

        if (null == map) {
            ConcurrentMap<Integer, ConsumeQueue> newMap = new ConcurrentHashMap<Integer, ConsumeQueue>(128);
            ConcurrentMap<Integer, ConsumeQueue> oldMap = consumeQueueTable.putIfAbsent(topic, newMap);
            if (oldMap != null) {
                map = oldMap;
            } else {
                map = newMap;
            }
        }

        ConsumeQueue logic = map.get(queueId);

        //说明topic的queue还没有初始化，需要初始化
        if(null == logic){
            ConsumeQueue newLogic = new ConsumeQueue(this,topic,queueId,
                    MessageStoreConfig.getStorePathConsumeQueue(),
                    MessageStoreConfig.getMappedFileSizeConsumeQueue());
            ConsumeQueue oldLogic = map.putIfAbsent(queueId, newLogic);
            if (oldLogic != null) {
                logic = oldLogic;
            } else {
                logic = newLogic;
            }
        }
        return logic;
    }

    public void putMessagePositionInfo(DispatchRequest dispatchRequest){
        //这里先获取consumeQueue，但是不是使用getConsumeQueue，而是需要新建一个方法
        ConsumeQueue consumeQueue = findConsumeQueue(dispatchRequest.getTopic(), dispatchRequest.getQueueId());
        consumeQueue.putMessagePositionInfoWrapper(dispatchRequest);
    }

    public CommitLog getCommitLog() {
        return commitLog;
    }

    public void doDispatch(DispatchRequest req) {
        for (CommitLogDispatcher dispatcher : this.dispatcherList) {
            dispatcher.dispatch(req);
        }
    }


    public class CommitLogDispatcherBuildConsumeQueue implements CommitLogDispatcher{
        @Override
        public void dispatch(DispatchRequest request) {
            //这里是把request中的offset写进consumeQueue中的
            DefaultMessageStore.this.putMessagePositionInfo(request);
        }
    }



    /**
     * 不断得从commitLog拿到offset，然后写到consumeQueue中
     * 更新ConsumeQueue中消息偏移
     * 很显然这个线程是会被阻塞的，因为当commitLog没有最新数据时，是拿不到offset的
     */
    class RePutMessageService extends ServiceThread {

        /**
         * 记录了本次需要拉取的消息在CommitLog中的偏移，
         * 这个变量是本类维护的，而不是通过赋值获取的
         * 该变量在读完commitLog一条数据后就会往后移
         */
        private volatile long rePutFromOffset = 0;

        public long getRePutFromOffset() {
            return rePutFromOffset;
        }

        public void setRePutFromOffset(long rePutFromOffset) {
            this.rePutFromOffset = rePutFromOffset;
        }

        @Override
        public String getServiceName() {
            return RePutMessageService.class.getSimpleName();
        }

        private void doRePut(){

            //todo 这里肯定要有一个for循环，一直拿commitLog的数据，写offset
            //直到没有offset可以写为止
            //先获取commitLog的数据

            SelectMappedBufferResult result = DefaultMessageStore.this.commitLog.getData(rePutFromOffset);

            //根据result的buffer数据构造成dispatcherRequest

            DispatchRequest dispatchRequest = DefaultMessageStore.this.commitLog.
                    makeDispatcherRequest(result.getByteBuffer());

            //然后把offset写到consumeQueue中
            DefaultMessageStore.this.doDispatch(dispatchRequest);

            //todo 更新rePutFromOffset

        }

        @Override
        public void run() {
            //轮询调用doRePut
            while(true){
                try {
                    Thread.sleep(1);
                    this.doRePut();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
