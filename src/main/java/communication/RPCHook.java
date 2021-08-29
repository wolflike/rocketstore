package communication;

import communication.protocol.RemotingCommand;

/**
 * 远程通信钩子函数，通信之前或者之后可调用
 * 看到rocketmq的代码实现，主要是用于ACL
 * 比如在发起请求之前，看一下权限是否够
 * @author 28293
 */
public interface RPCHook {
    /**
     * 在发起请求前调用
     * @param remoteAddr
     * @param request
     */
    void doBeforeRequest(final String remoteAddr, final RemotingCommand request);

    /**
     * 在返回response之后调用
     * @param remoteAddr
     * @param request
     * @param response
     */
    void doAfterResponse(final String remoteAddr, final RemotingCommand request,
                         final RemotingCommand response);
}
