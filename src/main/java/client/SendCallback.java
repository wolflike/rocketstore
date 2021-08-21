package client;

import client.result.SendResult;

/**
 * @author 28293
 */
public interface SendCallback {
    /**
     * when success,invoke this func
     * @param sendResult
     */
    void onSuccess(final SendResult sendResult);

    /**
     * when exception,invoke this func
     * @param e
     */
    void onException(final Throwable e);
}
