package client;

import client.result.QueryResult;
import common.MessageQueue;
import message.MessageExt;

/**
 * 消息队列管理的基类
 * @author 28293
 */
public interface MQAdmin {

    /**
     * 创建一个topic
     * @param key accessKey
     * @param newTopic
     * @param queueNum
     */
    void createTopic(final String key,final String newTopic, final int queueNum);

    /**
     * 创建一个topic
     * @param key
     * @param newTopic
     * @param queueNum
     * @param topicSysFlag topic系统标签
     */
    void createTopic(String key,String newTopic,int queueNum,int topicSysFlag);

    /**
     * 从队列中获取最大offset
     * @param mq
     * @return
     */
    long maxOffset(final MessageQueue mq);

    /**
     * 从队列中获取最小offset
     * @param mq
     * @return
     */
    long minOffset(final MessageQueue mq);

    /**
     * 通过时间戳查找offset
     * @param mq
     * @param timestamp
     * @return
     */
    long searchOffset(final MessageQueue mq,final long timestamp);

    /**
     * 获取最早的已存储的消息时间
     * @param mq
     * @return
     */
    long earliestMsgStoreTime(final MessageQueue mq);

    /**
     * 查询消息
     * @param topic
     * @param key
     * @param maxNum
     * @param begin
     * @param end
     * @return
     */
    QueryResult queryMessage(final String topic,final String key,final int maxNum,final long begin,
                             final long end);

    /**
     * 通过messageId查询消息
     * @param offsetMsgId
     * @return
     */
    MessageExt viewMessage(final String offsetMsgId);

    /**
     * 根据messageId查询消息
     * @param topic
     * @param msgId
     * @return
     */
    MessageExt viewMessage(String topic,String msgId);

}
