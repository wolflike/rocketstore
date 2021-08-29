package communication.netty.processor;

import communication.protocol.RemotingCommand;
import io.netty.channel.ChannelHandlerContext;

/**
 * netty实现的处理器
 * @author 28293
 */
public interface NettyRequestProcessor {

    /**
     * 处理请求
     * @param ctx
     * @param request
     * @return
     */
    RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request);

    /**
     * 是否拒绝请求
     * @return
     */
    boolean rejectRequest();

}
