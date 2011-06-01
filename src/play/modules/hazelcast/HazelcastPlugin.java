package play.modules.hazelcast;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import play.Logger;
import play.Play;
import play.PlayPlugin;
import play.cache.Cache;
import play.cache.CacheImpl;
import play.classloading.ApplicationClasses.ApplicationClass;
import play.inject.BeanSource;
import play.inject.Injector;
import play.mvc.Router;
import play.vfs.VirtualFile;

import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.AtomicNumber;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IList;
import com.hazelcast.core.ILock;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.ISet;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.IdGenerator;
import com.hazelcast.core.MultiMap;
import com.hazelcast.core.Transaction;
import com.hazelcast.partition.PartitionService;

public class HazelcastPlugin extends PlayPlugin implements BeanSource, NamedBeanSource {

	private static HazelcastInstance instance;

	@Override
	public void onApplicationStart() {
		try {
			if (instance == null) {
				VirtualFile confXml = Play.getVirtualFile("conf/hazelcast.xml");
				if(confXml != null){
					Logger.info("Building Hazelcast Configuration for: %s", confXml.getName());
					XmlConfigBuilder conf = new XmlConfigBuilder(confXml.inputstream());
					instance = Hazelcast.init(conf.build());
				}else{
					Logger.info("Building Hazelcast Configuration using default values...");
					instance = Hazelcast.newHazelcastInstance(null);
				}
				Logger.info("Hazelcast Services are now started...\n");
			}
			Logger.info("Replacing EhCacheImpl with HazelcastCacheImpl...");
			Cache.stop();
			Cache.forcedCacheImpl = (CacheImpl) HazelcastCacheImpl.getInstance();
			Cache.init();
			Logger.info("Cache Impl: %s", Cache.cacheImpl.getClass().getName());

		} catch (Exception e) {
			throw new ExceptionInInitializerError(e);
		}
		Injector.inject(this);
		NamedInjector.inject(this);
	}
	
	@Override
	public void onApplicationStop() {
		try {
			Hazelcast.shutdownAll();
			instance = null;
			Logger.info("Hazelcast Services are now stopped\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onRoutesLoaded() {
		Router.prependRoute("GET", "/@hazel", "HazelcastApplication.index");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see play.inject.BeanSource#getBeanOfType(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public <T> T getBeanOfType(Class<T> clazz) {
		if (clazz.equals(HazelcastInstance.class)) {
			Logger.info("%s Injection...OK", clazz.getName());
			return (T) instance;
		}else if(clazz.equals(ExecutorService.class)){
			return (T) instance.getExecutorService();
		}else if(clazz.equals(Transaction.class)){
			return (T) instance.getTransaction();
		}else if(clazz.equals(PartitionService.class)){
			return (T) instance.getPartitionService();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see play.inject.NamedBeanSource#getBeanOfType(java.lang.Class, java.lang.String)
	 */
	public <T> T getBeanOfType(Class<T> clazz, String name) {
		if (clazz.equals(IMap.class) || clazz.equals(Map.class)) {
			return (T) instance.getMap(name);
		}else if(clazz.equals(IList.class) || clazz.equals(List.class)){
			return (T) instance.getList(name);
		}else if(clazz.equals(ExecutorService.class)){
			return (T) instance.getExecutorService(name);
		}else if(clazz.equals(ISet.class) || clazz.equals(Set.class)){
			return (T) instance.getSet(name);
		}else if(clazz.equals(IdGenerator.class)){
			return (T) instance.getIdGenerator(name);
		}else if(clazz.equals(AtomicNumber.class)){
			return (T) instance.getAtomicNumber(name);
		}else if(clazz.equals(MultiMap.class)){
			return (T) instance.getMultiMap(name);
		}else if(clazz.equals(IQueue.class) || clazz.equals(Queue.class)){
			return (T) instance.getQueue(name);
		}else if(clazz.equals(ITopic.class)){
			return (T) instance.getTopic(name);
		}
		return null;
	}
	
	public static HazelcastInstance getHazel(){
		return instance;
	}
	
	public static Transaction getTransaction(){
		return instance.getTransaction();
	}

	public static ILock getLock(Object o){
		return instance.getLock(o);
	}
	
}
