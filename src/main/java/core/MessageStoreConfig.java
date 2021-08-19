package core;

import core.ConsumeQueue;

import java.io.File;

/**
 * @author luo
 */
public class MessageStoreConfig {
    private static String storePathRootDir = System.getProperty("user.home") + File.separator + "store";
    private static String storePathCommitLog = System.getProperty("user.home") + File.separator + "store"
            + File.separator + "commitlog";
    private static String storePathConsumeQueue = System.getProperty("user.home") + File.separator + "store"
            + File.separator + "consumequeue";

    public static int flushIntervalCommitLog = 500;

    public static boolean flushCommitLogTimed = false;

    public static int syncFlushTimeout = 5_000;

    public static int syncCommitTimeout = 2_000;

    /**
     * How many pages are to be flushed when flush CommitLog
     */
    public static int flushCommitLogLeastPages = 4;

    /**
     * How many pages are to be committed when commit data to file
     */
    public static int commitCommitLogLeastPages = 4;

    private static String topicDefaultGroup = "default";

    public static int messageSize = 1024 * 1024 * 4;

    private static FlushDiskType flushDiskType = FlushDiskType.SYNC_FLUSH;

    public static FlushDiskType getFlushDiskType() {
        return flushDiskType;
    }

    /**
     * core.CommitLog file size,default is 1G
     */
    private static int mappedFileSizeCommitLog = 1024 * 1024 * 1024;
    /**
     * core.ConsumeQueue file size,default is 30W
     */
    private static int mappedFileSizeConsumeQueue = 300000 * ConsumeQueue.CQ_STORE_UNIT_SIZE;

    public static int getMappedFileSizeCommitLog() {
        return mappedFileSizeCommitLog;
    }

    public static int getMappedFileSizeConsumeQueue() {
        return mappedFileSizeConsumeQueue;
    }

    public static String getStorePathRootDir() {
        return storePathRootDir;
    }

    public static String getStorePathCommitLog() {
        return storePathCommitLog;
    }

    public static String getStorePathConsumeQueue() {
        return storePathConsumeQueue;
    }

    public static String getTopicDefaultGroup() {
        return topicDefaultGroup;
    }
}
