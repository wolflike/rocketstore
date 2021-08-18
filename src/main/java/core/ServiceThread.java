package core;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author luo
 * 存储引擎的服务线程基础类（重要）
 */
@Slf4j
public abstract class ServiceThread implements Runnable{
    private Thread thread;
    /**
     * 发射员，主要包括await和countDown
     * 当线程执行完后可以执行countDown 减1
     * 另外一个线程执行await，阻塞等待所有线程
     * 当所有线程都调用了countDown
     * 那个线程也就可以继续执行了
     */
    protected final CountDownLatch waitPoint = new CountDownLatch(1);
    protected volatile AtomicBoolean hasNotified = new AtomicBoolean(false);
    /**
     * 用来标记线程是running还是stopped
     */
    protected volatile boolean stopped = false;
    protected boolean isDaemon = false;

    /**
     * Make it able to restart the thread
     */
    private final AtomicBoolean started = new AtomicBoolean(false);


    /**
     * 获取serviceName
     * @return
     */
    public abstract String getServiceName();


    /**
     * 只有当前实例还没有启动线程，或者线程kill的时候
     * 他才会启动线程，同时设置线程是否为守护线程（有些服务是长时间的，必然要设为守护线程）
     */
    public void start(){
        //只有线程没有启动的时候，才会启动本线程（也就是说，多次启动，只会启动一次）
        if (!started.compareAndSet(false, true)) {
            return;
        }
        stopped = false;
        this.thread = new Thread(this, getServiceName());
        //设置为守护线程
        this.thread.setDaemon(isDaemon);
        this.thread.start();
    }

    public void shutdown() {
        this.shutdown(false);
    }

    public void shutdown(final boolean interrupt) {

        log.info("Try to shutdown service thread:{} started:{} lastThread:{}", getServiceName(), started.get(), thread);
        //只有当线程是启动的时候才会shutdown
        if (!started.compareAndSet(true, false)) {
            return;
        }
        this.stopped = true;
        log.info("shutdown thread " + this.getServiceName() + " interrupt " + interrupt);

        if (hasNotified.compareAndSet(false, true)) {
            //数值减1，相当于该线程完成事情了
            waitPoint.countDown(); // notify
        }

        if (interrupt) {
            this.thread.interrupt();

//            try {
//
//            } catch (InterruptedException e) {
//                log.error("Interrupted", e);
//            }
        }

    }

    /**
     * 当线程执行完某个动作，count减1
     */
    public void wakeup() {
        //hasNotified是用来控制waitPoint的await和countDown
        //当还没通知的时候，说明要通知了 countDown 减1
        if (hasNotified.compareAndSet(false, true)) {
            waitPoint.countDown(); // notify
        }
    }

    public boolean isStopped(){
        return stopped;
    }


    /**
     * 这个方法明显是在等待
     * 所以这个线程本质就是自己和自己合作
     * @param interval
     */
    protected void waitForRunning(long interval) {
        //当已通知的时候
        if (hasNotified.compareAndSet(true, false)) {
            this.onWaitEnd();
            return;
        }

        //entry to wait
        //todo 需要实现将count数值重置
        //waitPoint.reset();

        try {
            waitPoint.await(interval, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.error("Interrupted", e);
        } finally {
            hasNotified.set(false);
            this.onWaitEnd();
        }
    }

    protected void onWaitEnd() {
    }
}
