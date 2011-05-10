import org.junit.Test;

import play.test.UnitTest;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.MessageListener;
import com.hazelcast.core.Transaction;


public class TopicTest extends UnitTest{

	private static HazelcastInstance hazel = Hazelcast.getDefaultInstance();
	
	@Test
	public void testTopicSend(){
		ITopic<String> topic = hazel.getTopic("myTopic");
		topic.publish("testTopicSend");
		topic.addMessageListener(new MessageListener<String>() {
			public void onMessage(String msg) {
				assertEquals("testTopicSend", msg);
			}
		});
	}
	
	@Test
	public void testTopicSendTx(){
		Transaction tx = hazel.getTransaction();
		ITopic<String> topic = hazel.getTopic("testTopicSendTx");
		tx.begin();
		topic.publish("testTopicSendTx");
		tx.commit();
		topic.addMessageListener(new MessageListener<String>() {
			public void onMessage(String msg) {
				assertEquals("testTopicSendTx", msg);
			}
		});
		
	}
	
	@Test
	public void testTopicSendTxRollback(){
		Transaction tx = hazel.getTransaction();
		ITopic<String> topic = hazel.getTopic("testTopicSendTx");
		tx.begin();
		topic.publish("testTopicSendTx");
		tx.rollback();
		topic.addMessageListener(new MessageListener<String>() {
			public void onMessage(String msg) {
				assertEquals("testTopicSendTx", msg);
			}
		});
	}

}
