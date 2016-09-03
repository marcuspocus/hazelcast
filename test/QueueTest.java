import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.ItemEvent;
import com.hazelcast.core.ItemListener;
import com.hazelcast.transaction.TransactionContext;
import org.junit.Test;
import play.Logger;
import play.modules.hazelcast.HazelcastPlugin;
import play.test.UnitTest;

import java.util.Iterator;

public class QueueTest extends UnitTest {
    private HazelcastInstance hazel = HazelcastPlugin.getHazel();

    @Test
    public void testQueueSend() {
        IQueue<String> queue = hazel.getQueue("testQueueSend");
        queue.addItemListener(new ItemListener<String>() {
            @Override
            public void itemAdded(ItemEvent<String> msg) {
                Logger.info("Added msg: %s", msg.getItem());
            }

            @Override
            public void itemRemoved(ItemEvent<String> msg) {
                Logger.info("Removed msg: %s", msg.getItem());
            }
        }, true);
        for (int i = 0; i < 100; i++) {
            queue.add("item-" + i);
        }
        for (String item : queue) {
            queue.remove(item);
        }
    }

    @Test
    public void testQueueSendTx() {
        TransactionContext tx = hazel.newTransactionContext();
        tx.beginTransaction();
        IQueue<String> queue = hazel.getQueue("testQueueSend");
        queue.addItemListener(new ItemListener<String>() {
            @Override
            public void itemAdded(ItemEvent<String> msg) {
                Logger.info("Added msg: %s", msg.getItem());
            }

            @Override
            public void itemRemoved(ItemEvent<String> msg) {
                Logger.info("Removed msg: %s", msg.getItem());
            }
        }, true);
        for (int i = 0; i < 100; i++) {
            queue.add("item-" + i);
        }
        tx.commitTransaction();
        tx = hazel.newTransactionContext();
        tx.beginTransaction();
        for (Iterator<String> iterator = queue.iterator(); iterator.hasNext(); ) {
            queue.remove(iterator.next());
        }
        tx.commitTransaction();
    }
}
