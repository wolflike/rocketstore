package communication;

import communication.netty.processor.NettyRequestProcessor;
import communication.protocol.RemotingCommand;

import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * @author 28293
 */
public interface RemotingClient extends RemotingService{

    /**
     * 注册processor
     * @param requestCode
     * @param processor
     * @param executor
     */
    void registerProcessor(final int requestCode, final NettyRequestProcessor processor, final ExecutorService executor);

    /**
     * 当nameserver有变更时，更新nameServer列表
     * @param addrs
     */
    void updateNameServerAddressList(final List<String> addrs);    //获取 NameServer 地址

    /**
     * 获取nameServer列表
     * @return
     */
    List<String> getNameServerAddressList();

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
