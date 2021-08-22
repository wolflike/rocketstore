package communication.netty;

import lombok.Getter;

/**
 * @author 28293
 */
@Getter
public class NettyClientConfig {

    /**
     * Worker thread number
     */
    private int clientWorkerThreads = 4;
    private int clientCallbackExecutorThreads = Runtime.getRuntime().availableProcessors();


}
