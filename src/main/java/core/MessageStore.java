package core;

import message.Message;
import message.MessageExtBrokerInner;
import result.GetMessageResult;
import result.PutMessageResult;
import result.QueryMessageResult;
import result.SelectMappedBufferResult;

import java.util.List;

/**
 * @author luo
 */
public interface MessageStore {

    /**
     * Load previously stored messages.
     */
    boolean load();

    /**
     * Launch this message store.
     *
     * @throws Exception if there is any error.
     */
    void start() throws Exception;

    /**
     * Shutdown this message store.
     */
    void shutdown();

    /**
     * Destroy this message store. Generally, all persistent files should be removed after invocation.
     */
    void destroy();

    /**
     * Store a message into store.
     *
     * @param msg message.Message instance to store
     * @return result of store operation.
     */
    PutMessageResult putMessage(MessageExtBrokerInner msg);

    /**
     * Store a batch of messages.
     *
     * @param messages message.Message batch.
     * @return result of storing batch messages.
     */
    PutMessageResult putMessages(final List<Message> messages);

    /**
     * Query at most <code>maxMsgNums</code> messages belonging to <code>topic</code> at <code>queueId</code> starting
     * from given <code>offset</code>. Resulting messages will further be screened using provided message filter.
     *
     * @param group Consumer group that launches this query.
     * @param topic Topic to query.
     * @param queueId Queue ID to query.
     * @param offset Logical offset to start from.
     * @param maxMsgNums Maximum count of messages to query.
     * @return Matched messages.
     */
    GetMessageResult getMessage(final String group, final String topic, final int queueId,
                                final long offset, final int maxMsgNums);

    /**
     * Look up the message by given commit log offset.
     *
     * @param commitLogOffset physical offset.
     * @return message.Message whose physical offset is as specified.
     */
    Message lookMessageByOffset(final long commitLogOffset);

    /**
     * Get one message from the specified commit log offset.
     *
     * @param commitLogOffset commit log offset.
     * @return wrapped result of the message.
     */
    SelectMappedBufferResult selectOneMessageByOffset(final long commitLogOffset);

    /**
     * Get one message from the specified commit log offset.
     *
     * @param commitLogOffset commit log offset.
     * @param msgSize message size.
     * @return wrapped result of the message.
     */
    SelectMappedBufferResult selectOneMessageByOffset(final long commitLogOffset, final int msgSize);

    /**
     * Get the raw commit log data starting from the given offset, which should used for replication purpose.
     *
     * @param offset starting offset.
     * @return commit log data.
     */
    SelectMappedBufferResult getCommitLogData(final long offset);

    /**
     * 通过key查找消息
     * @param topic topic of the message.
     * @param key message key.
     * @param maxNum maximum number of the messages possible.
     * @param begin begin timestamp.
     * @param end end timestamp.
     * @return result to query
     */
    QueryMessageResult queryMessage(final String topic, final String key, final int maxNum, final long begin,
                                    final long end);


    /**
     * Get consume queue of the topic/queue.
     *
     * @param topic Topic.
     * @param queueId Queue ID.
     * @return Consume queue.
     */
    ConsumeQueue getConsumeQueue(String topic, int queueId);
}

