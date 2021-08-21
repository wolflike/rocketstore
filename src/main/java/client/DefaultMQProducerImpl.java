package client;

import client.result.RequestCallback;
import client.result.SendResult;
import common.MessageQueue;
import communication.CommunicationMode;
import message.Message;

/**
 * @author 28293
 */
public class DefaultMQProducerImpl implements MQProducerInner{

    private MQClientInstance mQClientFactory;


    public Message request(Message msg,
                           long timeout){
        sendDefaultImpl(msg, CommunicationMode.ASYNC, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {

            }

            @Override
            public void onException(Throwable e) {

            }
        },timeout);
        return null;
    }
    public Message request(Message msg,
                           RequestCallback callback,
                           long timeout){
        //callback的作用就是传入一个接口匿名实现类
        //当发请求成功或者失败时，会调用回调函数
        return null;
    }
    private SendResult sendDefaultImpl(
            Message msg,
            final CommunicationMode communicationMode,
            final SendCallback sendCallback,
            final long timeout
    ){
        //todo
        //sendKernelImpl
        return null;
    }

    private SendResult sendKernelImpl(final Message msg,
                                      final MessageQueue mq,
                                      final CommunicationMode communicationMode,
                                      final SendCallback sendCallback,
                                      final long timeout){

        switch (communicationMode){
            case ASYNC:
//                mQClientFactory.getMQClientAPIImpl().sendMessage()
        }

        return null;

    }


}
