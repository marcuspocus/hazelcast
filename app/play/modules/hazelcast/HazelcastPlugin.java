package play.modules.hazelcast;

import javax.inject.Inject;
import javax.inject.Named;

import play.Logger;
import play.PlayPlugin;
import play.cache.Cache;
import play.inject.BeanSource;
import play.inject.Injector;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import play.mvc.Router;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class HazelcastPlugin extends PlayPlugin implements BeanSource {

	private static HazelcastInstance instance;

	@Override
	public void onApplicationStart() {
		try {
			if (instance == null) {
				instance = Hazelcast.getDefaultInstance();
				if(!instance.getLifecycleService().isRunning()){
					Logger.info("Hazelcast Services is restarting...\n");
					instance.getLifecycleService().restart();
				}
				Logger.info("Hazelcast Services are now started...\n");
			}

		} catch (Exception e) {
			throw new ExceptionInInitializerError(e);
		}
		Injector.inject(this);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onApplicationStop() {
		try {
			instance.shutdown();
			instance = null;
			Logger.info("Hazelcast Services are now stopped\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		}
		Logger.info("%s Injection...KO", clazz.getName());
		return null;
	}

	@Override
	public boolean rawInvocation(Request request, Response response) throws Exception {
		if ("/@cache".equals(request.path)) {
			response.status = 302;
			response.setHeader("Location", "/@cache/");
			return true;
		}
		return false;
	}

	@Override
	public void onRoutesLoaded() {
		Router.prependRoute("GET", "/@cache/?", "HazelcastApplication.index");
	}

}
