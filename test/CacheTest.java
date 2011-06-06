
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import play.cache.Cache;
import play.modules.hazelcast.HazelcastCacheImpl;
import play.test.UnitTest;


public class CacheTest extends UnitTest {

	@Before
	public void before(){
		Cache.clear();
	}
	
	@After
	public void after(){
		Cache.clear();
	}
	
	@Test
	public void testCacheName(){
		String cacheName = Cache.cacheImpl.getClass().getName();
		assertEquals("Instance Cache is NOT OK", HazelcastCacheImpl.class.getName(), cacheName);
	}

	@Test
	public void testCache(){
		Cache.add("test", "bidon de daube");
		String test = Cache.get("test", String.class);
		assertEquals("bidon de daube", test);
	}
	
	@Test
	public void testIncr(){
		long a = Cache.incr("counteur");
		assertTrue(a > 0);
	}
	
	@Test
	public void testDecr(){
		long a = Cache.decr("counteur");
		assertTrue(a == 0);
	}

	@Test
	public void testIncrBy2(){
		long a = Cache.incr("counteur", 2);
		assertTrue(a == 2);
	}
	
	@Test
	public void testDecrBy2(){
		long a = Cache.decr("counteur", 2);
		assertTrue(a == 0);
	}

	@Test
	public void testIncrBy10(){
		long a = Cache.incr("counteur", 10);
		assertTrue(a == 10);
	}
	
	@Test
	public void testDecrBy5(){
		long a = Cache.decr("counteur", 5);
		assertTrue(a == 5);
	}
	
	@Test
	public void testDecrBy5Again(){
		long a = Cache.decr("counteur", 5);
		assertTrue(a == 0);
	}

	@Test
	public void testDelete(){
		Cache.delete("counteur");
		assertNull(Cache.get("counteur"));
	}

	@Test
	public void testAddExisting(){
		Cache.add("testAddExisting", "value1");
		Cache.add("testAddExisting", "value2");
		String actual = Cache.get("testAddExisting", String.class);
		assertEquals("value1", actual);
	}
	
	@Test
	public void testReplace(){
		Cache.add("testReplace", "value1");
		Cache.replace("testReplace", "value2");
		String actual = Cache.get("testReplace", String.class);
		assertEquals("value2", actual);
	}
	
	@Test
	public void testExpiration(){
		Cache.add("testExpiration", "testExpiration", "1s");
		try {
			Thread.sleep(5000);
		} catch (Exception e) {
		}
		String actual = Cache.get("testExpiration", String.class);
		assertNull(String.format("actual: %s", actual), actual);
	}
	
}
