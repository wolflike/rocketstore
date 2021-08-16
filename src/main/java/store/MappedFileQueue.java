package store;

import store.MappedFile;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author luo
 *
 */
public class MappedFileQueue {

    private final String storePath;

    private final int mappedFileSize;

    private final CopyOnWriteArrayList<MappedFile> mappedFiles = new CopyOnWriteArrayList<MappedFile>();


    public MappedFileQueue(String storePath, int mappedFileSize) {
        this.storePath = storePath;
        this.mappedFileSize = mappedFileSize;
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

    /**
     * 刷新数据到磁盘中
     * @param flushLeastPages
     * @return
     */
    public boolean flush(final int flushLeastPages) {
        boolean result = true;
//        MappedFile mappedFile = this.findMappedFileByOffset(this.flushedWhere, this.flushedWhere == 0);
//        if (mappedFile != null) {
//            long tmpTimeStamp = mappedFile.getStoreTimestamp();
//            int offset = mappedFile.flush(flushLeastPages);
//            long where = mappedFile.getFileFromOffset() + offset;
//            result = where == this.flushedWhere;
//            this.flushedWhere = where;
//            if (0 == flushLeastPages) {
//                this.storeTimestamp = tmpTimeStamp;
//            }
//        }

        return result;
    }
}
