package client;

import client.result.RequestCallback;
import message.Message;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author 28293
 */
public class RequestResponseFuture {

    private final String correlationId;
    private final RequestCallback requestCallback;
    private final Message requestMsg = null;
    private long timeoutMillis;

    private CountDownLatch countDownLatch = new CountDownLatch(1);

    private volatile Message responseMsg = null;



    public RequestResponseFuture(String correlationId, long timeoutMillis, RequestCallback requestCallback) {
        this.correlationId = correlationId;
        this.timeoutMillis = timeoutMillis;
        this.requestCallback = requestCallback;
    }

    public Message waitResponseMessage(final long timeout) throws InterruptedException {
        this.countDownLatch.await(timeout, TimeUnit.MILLISECONDS);
        return this.responseMsg;
    }
}
