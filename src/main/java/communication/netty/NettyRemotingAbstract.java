package communication.netty;

import communication.protocol.RemotingCommand;
import io.netty.channel.Channel;

import java.net.SocketAddress;

/**
 * @author 28293
 */
public abstract class NettyRemotingAbstract {


    public RemotingCommand invokeSyncImpl(final Channel channel, final RemotingCommand request,
                                          final long timeoutMillis)
            throws Exception{

        final SocketAddress addr = channel.remoteAddress();

        final ResponseFuture responseFuture = new ResponseFuture();


        channel.writeAndFlush(request).addListener((channelFuture) ->{
            if (channelFuture.isSuccess()) {
                responseFuture.setSendRequestOK(true);
                return;
            } else {
                responseFuture.setSendRequestOK(false);
            }

            responseFuture.putResponse(null);
        });

        RemotingCommand responseCommand = responseFuture.waitResponse(timeoutMillis);
        if (null == responseCommand){
            throw new Exception();
        }
        return responseCommand;

    }

}
