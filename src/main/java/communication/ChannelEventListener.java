package communication;

import io.netty.channel.Channel;

/**
 * @author 28293
 */
public interface ChannelEventListener {
    /**
     * 当channel 连接时 调用
     * @param remoteAddr
     * @param channel
     */
    void onChannelConnect(final String remoteAddr, final Channel channel);

    /**
     * 当channel关闭时 调用
     * @param remoteAddr
     * @param channel
     */
    void onChannelClose(final String remoteAddr, final Channel channel);

    /**
     * 当channel发生异常时 调用
     * @param remoteAddr
     * @param channel
     */
    void onChannelException(final String remoteAddr, final Channel channel);

    /**
     * 当channel idle时 调用
     * @param remoteAddr
     * @param channel
     */
    void onChannelIdle(final String remoteAddr, final Channel channel);
}
