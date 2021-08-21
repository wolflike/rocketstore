package communication.netty;

import communication.InvokeCallback;
import communication.RPCHook;
import communication.RemotingClient;
import communication.protocol.RemotingCommand;

/**
 * @author 28293
 */
public class NettyRemotingClient extends NettyRemotingAbstract implements RemotingClient {
    @Override
    public RemotingCommand invokeSync(String addr, RemotingCommand request, long timeoutMillis) {
        return null;
    }

    @Override
    public void invokeAsync(String addr, RemotingCommand request, long timeoutMillis, InvokeCallback invokeCallback) {

    }

    @Override
    public void invokeOneway(String addr, RemotingCommand request, long timeoutMillis) {

    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public void registerRPCHook(RPCHook rpcHook) {

    }
}
