import java.io.Serializable;
import java.util.concurrent.Callable;

import play.modules.hazelcast.HazelcastPlugin;

public class MyCallable implements Callable<Long>, Serializable {
	private static final long serialVersionUID = 1L;
	public String name;

	public MyCallable(String name) {
		this.name = name;
	}

	public Long call() throws Exception {
		return HazelcastPlugin.getHazel().getIdGenerator(name).newId();
	}
}