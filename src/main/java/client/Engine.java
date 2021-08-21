package client;

import core.DefaultMessageStore;
import core.MessageStore;
import core.MessageStoreConfig;
import message.MessageExt;
import message.MessageExtBrokerInner;
import result.GetMessageResult;
import result.PutMessageResult;

import java.nio.ByteBuffer;
import java.util.Map;

/**
 * @author luo
 * 要求：
 * 性能评测
 *
 * 评测程序会创建20~40个线程，每个线程随机若干个topic（topic总数<=100），
 * 每个topic有N个queueId（1 <= N <= 10,000），持续调用append接口进行写入；
 * 评测保证线程之间数据量大小相近（topic之间不保证），每个data的大小为100B-17KiB区间随机
 * （伪随机数程度的随机），数据几乎不可压缩，需要写入总共150GiB的数据量。（每个线程写5G左右的数据）
 *
 * 保持刚才的写入压力，随机挑选50%的队列从当前最大点位开始读取，
 * 剩下的队列均从最小点位开始读取（即从头开始），再写入总共100GiB后停止全部写入，读取持续到没有数据，然后停止。
 *
 * 评测考察维度包括整个过程的耗时。超时为1800秒，超过则强制退出。
 *
 * 最终排行开始时会更换评测数据。
 *
 * 正确性评测
 *
 * 写入若干条数据。
 *
 * 重启ESC，并清空傲腾盘上的数据。
 *
 * 再读出来，必须严格等于之前写入的数据。
 *
 * 本程序主要实现ConsumeQueue、core.CommitLog
 */
public class Engine {

    private MessageStore store;

    public Engine(){
        store = new DefaultMessageStore();
    }


    /**
     * 创建一个topic，topic名唯一，不可重复
     * 使用mysql记录
     * 默认给topic创建10000个queue
     * @param topicName
     * @return
     */
    public boolean createTopic(String topicName){
        return true;
    }

    /**
     * 删除一个topic时，在mysql标记为删除
     * 通过一个轮询线程查询topic是否标记为删除
     * 然后删除相关的所有topic相关的文件和数据结构（包括index、consumeQueue、commitLog）
     * @return
     */
    public boolean deleteTopic(){
        return true;
    }


    /**
     * 在topic+queueId中写入data数据，返回offset
     * @param topic
     * @param queueId
     * @param data
     * @return
     */
    public long append(String topic, int queueId, ByteBuffer data){

        //这里简单通过MessageStore写入数据即可
        //todo 通过topic queueId data 构建message
        MessageExtBrokerInner message = new MessageExtBrokerInner();
        message.setTopic(topic);
        message.setQueueId(queueId);
        message.setBody(data.array());
        PutMessageResult putMessageResult = store.putMessage(message);
        return putMessageResult.getAppendMessageResult().getLogicsOffset();
    }

    /**
     * 在topic+queueId中从offset起，最多读取fetchNum个数据
     * 如果没有fetchNum个数据，future返回null
     * @param topic
     * @param queueId
     * @param offset
     * @param fetchNum
     * @return
     */
    public Map<Integer,ByteBuffer> getRange(String topic,int queueId,long offset,int fetchNum){
        GetMessageResult message = store.getMessage(MessageStoreConfig.getTopicDefaultGroup(),
                topic, queueId, offset, fetchNum);
        //todo 解析message，返回map数据
        return null;
    }
}
