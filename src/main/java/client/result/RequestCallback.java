package client.result;

/**
 * @author 28293
 */
public interface RequestCallback {
    /**
     * 请求成功时，调用该函数
     * @param sendResult
     */
    void onSuccess(SendResult sendResult);

    /**
     * 请求发生异常时，调用该函数
     * @param e
     */
    void onException(Throwable e);
}
