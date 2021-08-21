package client;

import message.Message;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author 28293
 */
public class Test {
    public static void main(String[] args) {

        String producerGroup = "please_rename_unique_group_name";
        String topic = "RequestTopic";
        long ttl = 3000;

        DefaultMQProducer producer = new DefaultMQProducer(producerGroup);
        producer.start();

        try {
            Message msg = new Message(topic,
                    "",
                    "Hello world".getBytes(StandardCharsets.UTF_8));

            long begin = System.currentTimeMillis();
            Message retMsg = producer.request(msg, ttl);
            long cost = System.currentTimeMillis() - begin;
            System.out.printf("request to <%s> cost: %d replyMessage: %s %n", topic, cost, retMsg);
        } catch (Exception e) {
            e.printStackTrace();
        }
        producer.shutdown();
    }

}
