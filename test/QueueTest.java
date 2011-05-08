import org.junit.Test;

import play.Logger;
import play.test.UnitTest;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.ItemListener;
import com.hazelcast.core.Transaction;


public class QueueTest extends UnitTest{

	private static HazelcastInstance hazel = Hazelcast.getDefaultInstance();
	
	@Test
	public void testQueueSend(){
		
		IQueue<String> queue = hazel.getQueue("testQueueSend");
		queue.addItemListener(new ItemListener<String>() {
			
			public void itemRemoved(String msg) {
				Logger.info("Removed msg: %s", msg);
			}
			
			public void itemAdded(String msg) {
				Logger.info("Added msg: %s", msg);
			}
		},true);
		
		for(int i = 0 ; i < 100 ; i++){
			queue.add("item-" + i);
		}
		
		for(String item : queue){
			queue.remove(item);
		}
		
	}
	
	@Test
	public void testQueueSendTx(){
		Transaction tx = hazel.getTransaction();
		tx.begin();
		
		IQueue<String> queue = hazel.getQueue("testQueueSend");
		queue.addItemListener(new ItemListener<String>() {
			
			public void itemRemoved(String msg) {
				Logger.info("Removed msg: %s", msg);
			}
			
			public void itemAdded(String msg) {
				Logger.info("Added msg: %s", msg);
			}
		},true);
		
		for(int i = 0 ; i < 100 ; i++){
			queue.add("item-" + i);
		}
		tx.commit();
		tx = hazel.getTransaction();
		tx.begin();
		for(String item : queue){
			queue.remove(item);
		}
		tx.commit();
	}
}
