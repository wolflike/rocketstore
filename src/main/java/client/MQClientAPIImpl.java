package client;

import client.result.SendResult;
import com.sun.deploy.config.ClientConfig;
import communication.CommunicationMode;
import communication.RemotingClient;
import communication.netty.NettyClientConfig;
import communication.netty.NettyRemotingClient;
import communication.netty.ResponseFuture;
import communication.protocol.RemotingCommand;
import io.netty.channel.Channel;
import message.Message;

import java.net.SocketAddress;


/**
 * @author 28293
 */
public class MQClientAPIImpl {

    private final RemotingClient remotingClient;

    public MQClientAPIImpl(final NettyClientConfig nettyClientConfig) {
        this.remotingClient = new NettyRemotingClient(nettyClientConfig,null);

    }

    public SendResult sendMessage(
            final String addr,
            final String brokerName,
            final Message msg,
            final long timeoutMillis,
            final CommunicationMode communicationMode,
            final SendCallback sendCallback,
            final MQClientInstance instance,
            final DefaultMQProducerImpl producer){

        RemotingCommand request = null;

        switch (communicationMode){
            case ONEWAY:
                this.remotingClient.invokeOneway(addr, request, timeoutMillis);
                return null;
            case SYNC:
                return sendMessageSync(addr,brokerName,msg,timeoutMillis,request);
            case ASYNC:
                sendMessageAsync(addr,brokerName,msg,timeoutMillis,request,sendCallback,instance,producer);
                return null;
            default:
                assert false;
                break;
        }

        return null;

    }
    private SendResult sendMessageSync(
            final String addr,
            final String brokerName,
            final Message msg,
            final long timeoutMillis,
            final RemotingCommand request
    ){
        //todo
        RemotingCommand response = remotingClient.invokeSync(addr, request, timeoutMillis);

        return null;
    }
    private void sendMessageAsync(
            final String addr,
            final String brokerName,
            final Message msg,
            final long timeoutMillis,
            final RemotingCommand request,
            final SendCallback sendCallback,
            final MQClientInstance instance,
            final DefaultMQProducerImpl producer
    ){
        remotingClient.invokeAsync(addr,request,timeoutMillis,(responseFuture) ->{

            //todo operationComplete
        });
    }




}
