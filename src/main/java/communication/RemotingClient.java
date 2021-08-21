package communication;

import communication.protocol.RemotingCommand;

/**
 * @author 28293
 */
public interface RemotingClient extends RemotingService{

    /**
     * 同步发送
     * @param addr
     * @param request
     * @param timeoutMillis
     * @return
     */
    RemotingCommand invokeSync(final String addr, final RemotingCommand request,
                               final long timeoutMillis);

    /**
     * 异步发送
     * @param addr
     * @param request
     * @param timeoutMillis
     * @param invokeCallback
     */
    void invokeAsync(final String addr, final RemotingCommand request,
                     final long timeoutMillis, final InvokeCallback invokeCallback);

    /**
     * oneway 请求
     * @param addr
     * @param request
     * @param timeoutMillis
     */
    void invokeOneway(final String addr, final RemotingCommand request, final long timeoutMillis);
}
