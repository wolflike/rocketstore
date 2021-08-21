package store;

import core.AllocateMappedFileService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import utils.UtilAll;

import java.io.File;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author luo
 *
 */
@Getter
@Setter
@Slf4j
public class MappedFileQueue {

    private final String storePath;

    private final int mappedFileSize;

    private final CopyOnWriteArrayList<MappedFile> mappedFiles = new CopyOnWriteArrayList<>();

    private final AllocateMappedFileService allocateMappedFileService;

    private long flushedWhere = 0;
    private long committedWhere = 0;

    private volatile long storeTimestamp = 0;


    public MappedFileQueue(String storePath, int mappedFileSize, AllocateMappedFileService allocateMappedFileService) {
        this.storePath = storePath;
        this.mappedFileSize = mappedFileSize;
        this.allocateMappedFileService = allocateMappedFileService;
    }

    /**
     *如何获取不到mappedFile需要生成mappedFile
     * @param startOffset
     * @param needCreate
     * @return
     */
    public MappedFile getLastMappedFile(final long startOffset, boolean needCreate){
        long createOffset = -1;
        MappedFile mappedFileLast = getLastMappedFile();

        //如果最后一个mappedFile都没有，说明队列就没有mappedFile
        //所以就从startOffset着手
        if(mappedFileLast == null){
            createOffset = startOffset - (startOffset % this.mappedFileSize);
        }

        if(mappedFileLast !=null && mappedFileLast.isFull()){
            createOffset = mappedFileLast.getFileFromOffset() + this.mappedFileSize;
        }

        if(createOffset!=-1&&needCreate){
            String nextFilePath = this.storePath + File.separator + UtilAll.offset2FileName(createOffset);
            String nextNextFilePath = this.storePath + File.separator +
                    UtilAll.offset2FileName(createOffset+mappedFileSize);
            MappedFile mappedFile = null;

            //创建mappedFile是系统调用，是可能出错的
            //todo 需要做异常处理
            if(allocateMappedFileService !=null){
                mappedFile = allocateMappedFileService.putRequestAndReturnMappedFile(nextFilePath,nextNextFilePath,mappedFileSize);
            }else{
                mappedFile = new MappedFile(nextFilePath,mappedFileSize);
            }

            if(mappedFile!=null){
                if(mappedFiles.isEmpty()){
                    mappedFile.setFirstCreateInQueue(true);
                }
                mappedFiles.add(mappedFile);
            }

            return mappedFile;
        }

        return mappedFileLast;
    }
    public MappedFile getLastMappedFile(final long startOffset){
        return getLastMappedFile(startOffset,true);
    }

    public MappedFile getLastMappedFile(){
        MappedFile mappedFileLast = null;

        while (!this.mappedFiles.isEmpty()) {
            try {
                mappedFileLast = this.mappedFiles.get(this.mappedFiles.size() - 1);
                break;
            } catch (IndexOutOfBoundsException e) {
                //continue;
            } catch (Exception e) {
                break;
            }
        }

        return mappedFileLast;
    }
    public MappedFile getFirstMappedFile() {
        MappedFile mappedFileFirst = null;

        if (!this.mappedFiles.isEmpty()) {
            try {
                mappedFileFirst = this.mappedFiles.get(0);
            } catch (IndexOutOfBoundsException e) {
                //ignore
            } catch (Exception e) {
                log.error("getFirstMappedFile has exception.", e);
            }
        }

        return mappedFileFirst;
    }

    /**
     * 刷新数据到磁盘中
     * @param flushLeastPages
     * @return
     */
    public boolean flush(final int flushLeastPages) {
        boolean result = true;
        //flushedWhere代表已经刷到的位置
        MappedFile mappedFile = this.findMappedFileByOffset(this.flushedWhere, this.flushedWhere == 0);
        if (mappedFile != null) {
            long tmpTimeStamp = mappedFile.getStoreTimestamp();
            //当前mappedFile刷新到的位置
            int offset = mappedFile.flush(flushLeastPages);
            //加上文件偏移量
            long where = mappedFile.getFileFromOffset() + offset;
            //第一次刷where比flushedWhere大
            //flushedWhere赋值为where
            //第二次刷就相等了
            result = where == this.flushedWhere;
            this.flushedWhere = where;
            if (0 == flushLeastPages) {
                this.storeTimestamp = tmpTimeStamp;
            }
        }

        return result;
    }

    /**
     * 提交最少的页（一页4k）
     * @param commitLeastPages 默认为4页
     * @return
     */
    public boolean commit(final int commitLeastPages){
        return true;
    }

    /**
     * 通过offset定位到mappedFile
     * @param offset
     * @param returnFirstOnNotFound
     * @return
     */
    public MappedFile findMappedFileByOffset(final long offset, final boolean returnFirstOnNotFound) {
        try {
            MappedFile firstMappedFile = this.getFirstMappedFile();
            MappedFile lastMappedFile = this.getLastMappedFile();
            if (firstMappedFile != null && lastMappedFile != null) {
                if (offset < firstMappedFile.getFileFromOffset() || offset >= lastMappedFile.getFileFromOffset() + this.mappedFileSize) {
                    log.warn("Offset not matched. Request offset: {}, firstOffset: {}, lastOffset: {}, mappedFileSize: {}, mappedFiles count: {}",
                            offset,
                            firstMappedFile.getFileFromOffset(),
                            lastMappedFile.getFileFromOffset() + this.mappedFileSize,
                            this.mappedFileSize,
                            this.mappedFiles.size());
                } else {
                    int index = (int) ((offset / this.mappedFileSize) - (firstMappedFile.getFileFromOffset() / this.mappedFileSize));
                    MappedFile targetFile = null;
                    try {
                        targetFile = this.mappedFiles.get(index);
                    } catch (Exception ignored) {
                    }

                    if (targetFile != null && offset >= targetFile.getFileFromOffset()
                            && offset < targetFile.getFileFromOffset() + this.mappedFileSize) {
                        return targetFile;
                    }

                    for (MappedFile tmpMappedFile : this.mappedFiles) {
                        if (offset >= tmpMappedFile.getFileFromOffset()
                                && offset < tmpMappedFile.getFileFromOffset() + this.mappedFileSize) {
                            return tmpMappedFile;
                        }
                    }
                }

                if (returnFirstOnNotFound) {
                    return firstMappedFile;
                }
            }
        } catch (Exception e) {
            log.error("findMappedFileByOffset Exception", e);
        }

        return null;
    }

}
