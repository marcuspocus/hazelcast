package controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.hazelcast.config.ExecutorConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IList;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.MessageListener;
import com.hazelcast.core.Transaction;

import play.Logger;
import play.cache.Cache;
import play.cache.CacheFor;
import play.mvc.Controller;
import play.utils.Utils;

public class HazelcastApplication extends Controller {

	@Inject
	private static HazelcastInstance hazel;

	public static void index() {

		String implementation = Cache.cacheImpl.getClass().getName();
		String name = hazel.getName();
		String port = "" + hazel.getConfig().getPort();
		
		Transaction tx = hazel.getTransaction();
		tx.begin();
		
		IList<String> list = hazel.getList("myList");
		if(list != null){
			for(int i = 0 ; i < 100 ; i++){
				list.add("item-" + i);
			}
		}
		
		IQueue<String> queue = hazel.getQueue("myQueue");
		for(int i = 0; i < 100 ; i++){
			queue.add("item-"+i);
		}
		
		IMap<String, Object> map = hazel.getMap("myMap");
		for(int i = 0; i < 100 ; i++){
			map.put("item-" + i, "value-" + i);
		}
		
		ITopic<String> topic = hazel.getTopic("myTopic");
		topic.addMessageListener(new MessageListener<String>() {
			
			public void onMessage(String msg) {
				Logger.info("msg: %s", msg);
			}
		});
		for(int i = 0; i < 100 ; i++){
			topic.publish("item-"+i);
		}
		
		
		tx.rollback();

		List<MapConfig> maps = new ArrayList<MapConfig>();
		for (MapConfig key : hazel.getConfig().getMapConfigs().values()) {
			maps.add(key);
		}
		Map<String, ExecutorConfig> ec = hazel.getConfig().getExecutorConfigMap();
		List<ExecutorConfig> executors = new ArrayList<ExecutorConfig>();
		for (ExecutorConfig key : ec.values()) {
			executors.add(key);
		}
		render(implementation, name, port, maps, executors);
	}

}
