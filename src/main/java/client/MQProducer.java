package client;

import client.result.RequestCallback;
import client.result.SendResult;
import message.Message;

/**
 * @author 28293
 */
public interface MQProducer extends MQAdmin{
    /**
     * 启动生产者
     */
    void start();

    /**
     * 关闭生产者
     */
    void shutdown();

    /**
     * 发送消息
     * @param msg
     * @return
     */
    SendResult send(final Message msg);

    /**
     * 发送消息,加超时机制
     * @param msg
     * @param timeout
     * @return
     */
    SendResult send(final Message msg, final long timeout);

    /**
     * 通过回调方式发送，不需要返回信息
     * @param msg
     * @param sendCallback
     */
    void send(final Message msg,final SendCallback sendCallback);

    /**
     * 请求消息
     * @param msg
     * @param timeout
     * @return
     */
    Message request(final Message msg, final long timeout);

    /**
     * 通过回调机制
     * @param msg
     * @param requestCallback
     * @param timeout
     */
    void request(final Message msg, final RequestCallback requestCallback, final long timeout);


}
