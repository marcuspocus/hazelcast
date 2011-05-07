package play.modules.hazelcast;

import play.Logger;
import play.PlayPlugin;
import play.inject.BeanSource;
import play.inject.Injector;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import play.mvc.Router;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class HazelcastPlugin extends PlayPlugin implements BeanSource {

	private static HazelcastInstance instance;
	
	@Override
	public void onApplicationStart() {
		try {
			if (instance == null) {
				instance = Hazelcast.getDefaultInstance();
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
			Logger.info("Hazelcast Services are now stopped\n");
		} catch (Exception e) {
			// TODO: handle exception
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

	public static HazelcastInstance getHazelcastInstance() {
		return instance;
	}

}
