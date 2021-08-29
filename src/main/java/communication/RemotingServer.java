package communication;


import communication.netty.processor.NettyRequestProcessor;
import communication.protocol.RemotingCommand;
import io.netty.channel.Channel;

import java.util.concurrent.ExecutorService;

/**
 *
 * @author 28293
 */
public interface RemotingServer extends RemotingService{

    /**
     * 注册processor
     * @param requestCode
     * @param processor
     * @param executor
     */
    void registerProcessor(final int requestCode, final NettyRequestProcessor processor, final ExecutorService executor);    //注册一个默认的处理器，当根据requestCode匹配不到处理器，则使用这个默认的处理器

    /**
     * 注册默认的processor
     * @param processor
     * @param executor
     */
    void registerDefaultProcessor(final NettyRequestProcessor processor, final ExecutorService executor);    //获取端口

    /**
     * 返回默认的监听端口号
     * @return
     */
    int localListenPort();
    /**
     * 服务端同步invoke
     * @param channel
     * @param request
     * @param timeoutMillis
     * @return
     */
    RemotingCommand invokeSync(final Channel channel, final RemotingCommand request,
                               final long timeoutMillis);


    /**
     * 服务端异步invoke
     * @param channel
     * @param request
     * @param timeoutMillis
     * @param invokeCallback
     */
    void invokeAsync(final Channel channel, final RemotingCommand request, final long timeoutMillis,
                     final InvokeCallback invokeCallback);

    /**
     * 服务端单向invoke
     * @param channel
     * @param request
     * @param timeoutMillis
     */
    void invokeOneway(final Channel channel, final RemotingCommand request, final long timeoutMillis);
}
