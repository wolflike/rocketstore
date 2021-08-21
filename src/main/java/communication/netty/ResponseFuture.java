package communication.netty;

import communication.protocol.RemotingCommand;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author 28293
 */
public class ResponseFuture {

    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    private volatile RemotingCommand responseCommand;
    private volatile boolean sendRequestOK = true;

    public void setSendRequestOK(boolean sendRequestOK) {
        this.sendRequestOK = sendRequestOK;
    }

    public RemotingCommand waitResponse(final long timeoutMillis) throws InterruptedException {
        this.countDownLatch.await(timeoutMillis, TimeUnit.MILLISECONDS);
        return this.responseCommand;
    }

    public void putResponse(final RemotingCommand responseCommand) {
        this.responseCommand = responseCommand;
        this.countDownLatch.countDown();
    }
}
