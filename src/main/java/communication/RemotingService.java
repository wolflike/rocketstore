package communication;

/**
 * @author 28293
 */
public interface RemotingService {
    /**
     * 启动远程服务
     */
    void start();

    /**
     * 关闭服务
     */
    void shutdown();

    /**
     * 注册RPC钩子
     * @param rpcHook
     */
    void registerRPCHook(RPCHook rpcHook);
}
