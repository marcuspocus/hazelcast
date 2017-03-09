import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import com.hazelcast.transaction.TransactionContext;
import org.junit.Test;
import play.modules.hazelcast.HazelcastPlugin;
import play.test.UnitTest;

public class TopicTest extends UnitTest {
    private HazelcastInstance hazel = HazelcastPlugin.getHazel();

    @Test
    public void testTopicSend() {
        ITopic<String> topic = hazel.getTopic("myTopic");
        topic.publish("testTopicSend");
        topic.addMessageListener(new MessageListener<String>() {
            @Override
            public void onMessage(Message<String> msg) {
                assertEquals("testTopicSend", msg.getMessageObject());
            }
        });
    }

    @Test
    public void testTopicSendTx() {
        TransactionContext tx = hazel.newTransactionContext();
        ITopic<String> topic = hazel.getTopic("testTopicSendTx");
        tx.beginTransaction();
        topic.publish("testTopicSendTx");
        tx.commitTransaction();
        topic.addMessageListener(new MessageListener<String>() {
            @Override
            public void onMessage(Message<String> msg) {
                assertEquals("testTopicSendTx", msg.getMessageObject());
            }
        });
    }

    @Test
    public void testTopicSendTxRollback() {
        TransactionContext tx = hazel.newTransactionContext();
        ITopic<String> topic = hazel.getTopic("testTopicSendTx");
        tx.beginTransaction();
        topic.publish("testTopicSendTx");
        tx.rollbackTransaction();
        topic.addMessageListener(new MessageListener<String>() {
            @Override
            public void onMessage(Message<String> msg) {
                assertEquals("testTopicSendTx", msg.getMessageObject());
            }
        });
    }
}
