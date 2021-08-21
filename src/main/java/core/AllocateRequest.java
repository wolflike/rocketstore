package core;

import lombok.Getter;
import lombok.Setter;
import store.MappedFile;

import java.util.concurrent.CountDownLatch;

/**
 * @author 28293
 */
@Setter
@Getter
public class AllocateRequest implements Comparable<AllocateRequest>{

    private String filePath;
    private int fileSize;

    private CountDownLatch countDownLatch = new CountDownLatch(1);
    private volatile MappedFile mappedFile = null;

    public AllocateRequest(String filePath,int fileSize){
        this.filePath = filePath;
        this.fileSize = fileSize;
    }

    @Override
    public int compareTo(AllocateRequest o) {
        return 0;
    }
}
