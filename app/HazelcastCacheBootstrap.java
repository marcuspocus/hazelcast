import play.Logger;
import play.cache.Cache;
import play.cache.CacheImpl;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.modules.hazelcast.HazelcastCacheImpl;
import play.modules.hazelcast.HazelcastPlugin;


@OnApplicationStart
public class HazelcastCacheBootstrap extends Job<Void>{

	public void doJob(){
		Logger.info("Replacing EhCacheImpl with HazelcastCacheImpl...");
		Cache.stop();
		Cache.forcedCacheImpl = (CacheImpl) HazelcastCacheImpl.getInstance();
		Cache.init();
		Logger.info("Cache Impl: %s", Cache.cacheImpl.getClass().getName());
	}
	
}
