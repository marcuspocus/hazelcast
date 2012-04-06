import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.junit.Test;

import play.Logger;
import play.modules.hazelcast.HazelcastPlugin;
import play.test.UnitTest;

import com.hazelcast.core.HazelcastInstance;

public class ExecutorTest extends UnitTest {

	private HazelcastInstance hazel = HazelcastPlugin.getHazel();

	@Test
	public void testRunnable() {
		ExecutorService es = hazel.getExecutorService("testRunnable");
		es.execute(new MyRunnable("testRunnable"));
	}

	@Test
	public void testMultipleRunnable() {
		ExecutorService es = hazel.getExecutorService("testMultipleRunnable");
		List<Future<?>> list = new ArrayList<Future<?>>();
		for (int i = 0; i < 100; i++) {
			list.add(es.submit(new MyRunnable("testMultipleRunnable")));
		}
		for (Future<?> task : list) {
			try {
				task.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
	}

	@Test
	public void testMultipleCallable() {
		ExecutorService es = hazel.getExecutorService("testMultipleCallable");
		List<Future<Long>> list = new ArrayList<Future<Long>>();
		for (int i = 0; i < 100; i++) {
			list.add(es.submit(new MyCallable("testMultipleCallable")));
		}
		for (Future<Long> id : list) {
			try {
				Logger.info("IdGenerator: %s", "" + id.get());
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
	}

}
