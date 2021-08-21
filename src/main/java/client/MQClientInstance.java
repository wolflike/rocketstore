package client;

import communication.netty.NettyClientConfig;

/**
 * @author 28293
 */
public class MQClientInstance {
    private final MQClientAPIImpl mQClientAPIImpl;
    private final NettyClientConfig nettyClientConfig;

    public MQClientInstance(){
        this.nettyClientConfig = new NettyClientConfig();
        mQClientAPIImpl = new MQClientAPIImpl(nettyClientConfig);
    }

    public MQClientAPIImpl getMQClientAPIImpl() {
        return mQClientAPIImpl;
    }

}
