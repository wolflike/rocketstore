package client;

import client.result.QueryResult;
import client.result.RequestCallback;
import client.result.SendResult;
import common.MessageQueue;
import message.Message;
import message.MessageExt;

/**
 * @author 28293
 */
public class DefaultMQProducer implements MQProducer{

    private String producerGroup;

    protected final transient DefaultMQProducerImpl defaultMQProducerImpl;

    public DefaultMQProducer(String producerGroup) {
        this.producerGroup = producerGroup;
        defaultMQProducerImpl = new DefaultMQProducerImpl();
    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public SendResult send(Message msg) {
        return null;
    }

    @Override
    public SendResult send(Message msg, long timeout) {
        return null;
    }

    @Override
    public void send(Message msg, SendCallback sendCallback) {

    }

    @Override
    public Message request(Message msg, long timeout) {
        return defaultMQProducerImpl.request(msg,timeout);
    }

    @Override
    public void request(Message msg, RequestCallback requestCallback, long timeout) {
        defaultMQProducerImpl.request(msg,requestCallback,timeout);
    }

    @Override
    public void createTopic(String key, String newTopic, int queueNum) {

    }

    @Override
    public void createTopic(String key, String newTopic, int queueNum, int topicSysFlag) {

    }

    @Override
    public long maxOffset(MessageQueue mq) {
        return 0;
    }

    @Override
    public long minOffset(MessageQueue mq) {
        return 0;
    }

    @Override
    public long searchOffset(MessageQueue mq, long timestamp) {
        return 0;
    }

    @Override
    public long earliestMsgStoreTime(MessageQueue mq) {
        return 0;
    }

    @Override
    public QueryResult queryMessage(String topic, String key, int maxNum, long begin, long end) {
        return null;
    }

    @Override
    public MessageExt viewMessage(String offsetMsgId) {
        return null;
    }

    @Override
    public MessageExt viewMessage(String topic, String msgId) {
        return null;
    }
}
