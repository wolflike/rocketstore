package communication;

import communication.netty.ResponseFuture;

/**
 * @author 28293
 */
public interface InvokeCallback {

    /**
     * 当调用完成时，调用该方法
     * @param responseFuture
     */
    void operationComplete(final ResponseFuture responseFuture);
}
