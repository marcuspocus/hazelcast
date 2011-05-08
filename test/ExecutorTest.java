import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.junit.Test;

import play.Logger;
import play.test.UnitTest;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;


public class ExecutorTest extends UnitTest {

	HazelcastInstance hazel = Hazelcast.getDefaultInstance();
	
	@Test
	public void testRunnable(){
		ExecutorService es = hazel.getExecutorService("testRunnable");
		es.execute(new Runnable() {
			public void run() {
				Logger.info("My testRunnable...");
			}
		});
	}
	
	@Test
	public void testMultipleRunnable(){
		ExecutorService es = hazel.getExecutorService("testMultipleRunnable");
		List<Future<?>> list = new ArrayList<Future<?>>();
		for(int i = 0 ; i < 100 ; i++){
			list.add(es.submit(new Runnable() {
				public void run() {
					Logger.info("My testMultipleRunnable...");
				}
			}));
		}
		es.shutdown();
	}
}
