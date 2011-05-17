package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import play.Logger;
import play.cache.Cache;
import play.data.validation.Required;
import play.mvc.Controller;

import com.hazelcast.config.ExecutorConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.QueueConfig;
import com.hazelcast.config.TopicConfig;
import com.hazelcast.core.HazelcastInstance;

public class HazelcastApplication extends Controller {

	@Inject
	private static HazelcastInstance hazel;

	public static void index() {
		Logger.info("Compteur: %s", hazel.getAtomicNumber("test").addAndGet(1));
		String implementation = Cache.cacheImpl.getClass().getName();
		String name = hazel.getName();
		String port = "" + hazel.getConfig().getPort();
		
		hazel.getList("myList");
		hazel.getQueue("myQueue");
		hazel.getMap("myMap");
		hazel.getTopic("myTopic");
		hazel.getAtomicNumber("myAtomicNumber");
		hazel.getExecutorService("myExecutorService");
		hazel.getIdGenerator("myIdGenerator");
		hazel.getMultiMap("myMultiMap");
		hazel.getSet("mySet");

		List<MapConfig> maps = new ArrayList<MapConfig>(hazel.getConfig().getMapConfigs().values());
		List<ExecutorConfig> executors = new ArrayList<ExecutorConfig>(hazel.getConfig().getExecutorConfigs());
		List<QueueConfig> queues = new ArrayList<QueueConfig>(hazel.getConfig().getQConfigs().values());
		List<TopicConfig> topics = new ArrayList<TopicConfig>(hazel.getConfig().getTopicConfigs().values());

		render(implementation, name, port, maps, executors, queues, topics);
	}
	
	
	public static void viewMap(@Required String name){
		Map map = hazel.getMap(name);
		Set<String> keys = map.keySet();
		render(name, keys, map);
	}

}
