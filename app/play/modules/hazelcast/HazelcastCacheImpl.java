package play.modules.hazelcast;

import java.io.NotSerializableException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import play.cache.CacheImpl;
import play.exceptions.CacheException;

import com.hazelcast.core.AtomicNumber;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class HazelcastCacheImpl implements CacheImpl {

	private static HazelcastCacheImpl instance = new HazelcastCacheImpl();
	private static HazelcastInstance manager;
	private static IMap<String, Object> cache;
	
	
	private HazelcastCacheImpl() {
		manager = Hazelcast.getDefaultInstance();
		cache = manager.getMap("default");
	}

	public static HazelcastCacheImpl getInstance() {
		return instance;
	}
	
	/**
	 * Expiration is in SECONDS
	 */
	public void add(String key, Object value, int expiration) {
		cache.putIfAbsent(key, value, expiration, TimeUnit.SECONDS);
	}

	public void clear() {
		cache.clear();
	}

	public long decr(String key, int by) {
		AtomicNumber count = manager.getAtomicNumber(key);
		count.set(count.get() - by);
		return count.get();
	}

	public void delete(String key) {
		cache.remove(key);
	}

	public Object get(String key) {
		return cache.get(key);
	}

	public Map<String, Object> get(String[] keys) {
		Map<String, Object> result = new HashMap<String, Object>(keys.length);
		for (String key : keys) {
			result.put(key, get(key));
		}
		return result;
	}

	public long incr(String key, int by) {
		AtomicNumber count = manager.getAtomicNumber(key);
		count.set(count.get() + by);
		return count.get();
	}

	public void replace(String key, Object value, int expiration) {
		if (cache.containsKey(key)) {
			cache.put(key, value, expiration, TimeUnit.SECONDS);
		}
	}

	public boolean safeAdd(String key, Object value, int expiration) {
		try {
			add(key, value, expiration);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean safeDelete(String key) {
		try {
			delete(key);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean safeReplace(String key, Object value, int expiration) {
		try {
			replace(key, value, expiration);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean safeSet(String key, Object value, int expiration) {
		try {
			set(key, value, expiration);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public void set(String key, Object value, int expiration) {
		cache.put(key, value, expiration, TimeUnit.SECONDS);
	}

	public void stop() {
		cache = null;
		manager = null;
		instance = null;
	}

	/**
	 * Utility that check that an object is serializable.
	 */
	static void checkSerializable(Object value) {
		if (value != null && !(value instanceof Serializable)) {
			throw new CacheException("Cannot cache a non-serializable value of type " + value.getClass().getName(), new NotSerializableException(value.getClass().getName()));
		}
	}

}
