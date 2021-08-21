package client;

/**
 * @author 28293
 */
public class MQClientInstance {
    private final MQClientAPIImpl mQClientAPIImpl;

    public MQClientInstance(){
        mQClientAPIImpl = new MQClientAPIImpl();
    }

    public MQClientAPIImpl getMQClientAPIImpl() {
        return mQClientAPIImpl;
    }

}
