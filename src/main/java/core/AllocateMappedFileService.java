package core;

import lombok.extern.slf4j.Slf4j;
import store.MappedFile;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Create MappedFile in advance
 * @author 28293
 */
@Slf4j
public class AllocateMappedFileService extends ServiceThread{

    private long waitTimeOut = 5_000;

    private DefaultMessageStore store;

    private ConcurrentMap<String,AllocateRequest> requestTable = new ConcurrentHashMap<>();

    private PriorityBlockingQueue<AllocateRequest> requestQueue = new PriorityBlockingQueue<>();

    private volatile boolean hasException = false;

    @Override
    public String getServiceName() {
        return AllocateMappedFileService.class.getSimpleName();
    }

    public AllocateMappedFileService(DefaultMessageStore store){
        this.store = store;
    }

    public MappedFile putRequestAndReturnMappedFile(String nextFilePath,String nextNextFilePath,int fileSize){
        int canSubmitRequests = 2;
        //todo 堆外操作
        //if(MessageStoreConfig.)

        AllocateRequest request = new AllocateRequest(nextFilePath,fileSize);

        /**
         * 代表还没有创建这个文件
         */
        boolean nextPutOK = this.requestTable.putIfAbsent(nextFilePath,request) == null;

        if(nextPutOK){
            if(canSubmitRequests <= 0){
                requestTable.remove(nextFilePath);
                return null;
            }
            boolean offerOK = requestQueue.offer(request);

            canSubmitRequests --;

        }

        AllocateRequest nextNextRequest = new AllocateRequest(nextNextFilePath,fileSize);
        boolean nextNextPutOK = this.requestTable.putIfAbsent(nextNextFilePath,nextNextRequest) == null;

        if(nextNextPutOK){
            if(canSubmitRequests <= 0){
                requestTable.remove(nextNextFilePath);
            }else{
                boolean offerOK = requestQueue.offer(nextNextRequest);
            }
        }

        if(hasException){
            return null;
        }
        AllocateRequest result = requestTable.get(nextFilePath);

        try {
            if(result !=null){
                boolean waitOK = result.getCountDownLatch().await(waitTimeOut, TimeUnit.MILLISECONDS);

                if(!waitOK){
                    return null;
                }else{
                    requestTable.remove(nextFilePath);
                    return request.getMappedFile();
                }
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void run() {
        log.info(getServiceName() + "service started");

        while (!isStopped()&&mMapOperation()){

        }
        log.info(getServiceName() + "service end");
    }

    private boolean mMapOperation(){
        boolean isSuccess = false;
        AllocateRequest request = null;
        try {
            request = requestQueue.take();
            AllocateRequest expectedRequest = requestTable.get(request.getFilePath());
            if(null == expectedRequest){
                log.warn("");
                return true;
            }
            if(expectedRequest != request){
                log.warn("");
                return true;
            }
            if(request.getMappedFile() ==null){

                MappedFile mappedFile = null;

                mappedFile = new MappedFile(request.getFilePath(),request.getFileSize());

                request.setMappedFile(mappedFile);

                this.hasException = false;

                isSuccess = true;
            }
        } catch (InterruptedException e) {
            log.warn("");
            hasException = true;
            return false;
        } finally {
            if(request != null && isSuccess){
                request.getCountDownLatch().countDown();
            }
        }
        return true;
    }
}
