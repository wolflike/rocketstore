package communication.netty;

import communication.ChannelEventListener;
import communication.InvokeCallback;
import communication.RPCHook;
import communication.RemotingClient;
import communication.protocol.RemotingCommand;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author 28293
 */

@Slf4j
public class NettyRemotingClient extends NettyRemotingAbstract implements RemotingClient {


    private final Bootstrap bootstrap = new Bootstrap();
    private final EventLoopGroup eventLoopGroupWorker;

    private final NettyClientConfig nettyClientConfig;



    private final Lock lockChannelTables = new ReentrantLock();

    private final ExecutorService publicExecutor;

    private ExecutorService callbackExecutor;

    private final ChannelEventListener channelEventListener;

    private DefaultEventExecutorGroup defaultEventExecutorGroup;





    public NettyRemotingClient(NettyClientConfig nettyClientConfig,
                               final ChannelEventListener channelEventListener){

        this.nettyClientConfig = nettyClientConfig;
        this.channelEventListener = channelEventListener;

        int publicThreadNums = nettyClientConfig.getClientCallbackExecutorThreads();
        if(publicThreadNums <= 0){
            publicThreadNums = 4;
        }

        publicExecutor = Executors.newFixedThreadPool(publicThreadNums, new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "NettyClientPublicExecutor_" + this.threadIndex.incrementAndGet());
            }
        });

        //使用ThreadFactory创建线程
        this.eventLoopGroupWorker = new NioEventLoopGroup(1, new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, String.format("NettyClientSelector_%d", this.threadIndex.incrementAndGet()));
            }
        });
    }


    @Override
    public RemotingCommand invokeSync(String addr, RemotingCommand request, long timeoutMillis) {

        Channel channel = getAndCreateChannel(addr);

        //doBefore
        try {
            RemotingCommand response = this.invokeSyncImpl(channel,request,timeoutMillis);
            return response;
        } catch (Exception e) {
            log.error("");
        }
        //doAfter
        return null;
    }

    private Channel getAndCreateChannel(final String addr){
        return null;
    }

    @Override
    public void invokeAsync(String addr, RemotingCommand request, long timeoutMillis, InvokeCallback invokeCallback) {

    }

    @Override
    public void invokeOneway(String addr, RemotingCommand request, long timeoutMillis) {

    }

    @Override
    public void start() {
        defaultEventExecutorGroup = new DefaultEventExecutorGroup(
                nettyClientConfig.getClientWorkerThreads(), new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "NettyClientWorkerThread_" + this.threadIndex.incrementAndGet());
            }
        }
        );

        Bootstrap handler = bootstrap.group(eventLoopGroupWorker).channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY,true)
                .option(ChannelOption.SO_KEEPALIVE,false);

    }

    @Override
    public void shutdown() {

    }

    @Override
    public void registerRPCHook(RPCHook rpcHook) {

    }
}
