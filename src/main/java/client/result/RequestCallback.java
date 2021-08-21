package client.result;

import message.Message;

/**
 * @author 28293
 */
public interface RequestCallback {
    /**
     * 请求成功时，调用该函数
     * @param message
     */
    void onSuccess(final Message message);

    /**
     * 请求发生异常时，调用该函数
     * @param e
     */
    void onException(final Throwable e);
}
