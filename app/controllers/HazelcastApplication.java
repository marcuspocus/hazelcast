package controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.hazelcast.config.ExecutorConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import play.cache.Cache;
import play.cache.CacheFor;
import play.mvc.Controller;
import play.utils.Utils;

public class HazelcastApplication extends Controller {
	
	@Inject
	private static HazelcastInstance cache;
	
	public static void index(){
		if(cache == null){
			renderText("Fucking cache is NULL!");
		}
		
		Cache.add("test", new Date());
		Date test = Cache.get("test", Date.class);
		System.out.println(Utils.getHttpDateFormatter().format(test));
		
		String implementation = "";
		if(Cache.cacheImpl != null){
			implementation = Cache.cacheImpl.getClass().getName();
		}else{
			implementation = "Why the fuck this thing is null...";
		}
		String name = cache.getName();
		String port = "" + cache.getConfig().getPort();

		List<MapConfig> maps = new ArrayList<MapConfig>();
		for(MapConfig key : cache.getConfig().getMapConfigs().values()){
			maps.add(key);
		}
		Map<String,ExecutorConfig> ec = cache.getConfig().getExecutorConfigMap();
		List<ExecutorConfig> executors = new ArrayList<ExecutorConfig>();
		for(ExecutorConfig key : ec.values()){
			executors.add(key);
		}
		render(implementation, name, port, maps, executors);
	}

}
