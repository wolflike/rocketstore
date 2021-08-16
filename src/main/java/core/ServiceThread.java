package core;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author luo
 * 存储引擎的服务线程基础类（重要）
 */
@Slf4j
public abstract class ServiceThread implements Runnable{
    private Thread thread;
    /**
     * 发射员
     */
    protected final CountDownLatch waitPoint = new CountDownLatch(1);
    protected volatile AtomicBoolean hasNotified = new AtomicBoolean(false);
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
        if (!started.compareAndSet(false, true)) {
            return;
        }
        stopped = false;
        this.thread = new Thread(this, getServiceName());
        this.thread.setDaemon(isDaemon);
        this.thread.start();
    }

    public void shutdown() {
        this.shutdown(false);
    }

    public void shutdown(final boolean interrupt) {

        log.info("Try to shutdown service thread:{} started:{} lastThread:{}", getServiceName(), started.get(), thread);
        if (!started.compareAndSet(true, false)) {
            return;
        }
        this.stopped = true;
        log.info("shutdown thread " + this.getServiceName() + " interrupt " + interrupt);

        if (hasNotified.compareAndSet(false, true)) {
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

    public void wakeup() {
        if (hasNotified.compareAndSet(false, true)) {
            waitPoint.countDown(); // notify
        }
    }
}
