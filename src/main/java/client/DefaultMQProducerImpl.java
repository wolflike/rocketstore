package client;

import client.result.RequestCallback;
import message.Message;

/**
 * @author 28293
 */
public class DefaultMQProducerImpl implements MQProducerInner{

    private MQClientInstance mQClientFactory;


    public Message request(Message msg,
                           long timeout){
        return null;
    }
    public Message request(Message msg,
                           RequestCallback callback,
                           long timeout){
        //callback的作用就是传入一个接口匿名实现类
        //当发请求成功或者失败时，会调用回调函数
        return null;
    }

}
