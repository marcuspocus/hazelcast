import play.Logger;
import play.cache.Cache;
import play.cache.CacheImpl;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.modules.hazelcast.HazelcastCacheImpl;


@OnApplicationStart
public class HazelcastCacheBootstrap extends Job<Void>{

	public void doJob(){
		Cache.stop();
		Cache.forcedCacheImpl = (CacheImpl) HazelcastCacheImpl.getInstance();
		Cache.init();
		Logger.info("Cache Impl: %s", Cache.cacheImpl.getClass().getName());
	}
	
}
