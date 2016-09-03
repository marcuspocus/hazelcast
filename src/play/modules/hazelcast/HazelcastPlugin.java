package play.modules.hazelcast;

import com.hazelcast.config.Config;
import com.hazelcast.config.XmlConfigBuilder;
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
import com.hazelcast.core.PartitionService;
import com.hazelcast.transaction.TransactionContext;
import com.hazelcast.transaction.TransactionOptions;
import play.Logger;
import play.Play;
import play.PlayPlugin;
import play.cache.Cache;
import play.inject.BeanSource;
import play.mvc.Router;
import play.vfs.VirtualFile;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

public class HazelcastPlugin extends PlayPlugin implements BeanSource, NamedBeanSource {
    private static HazelcastInstance instance;
    public static boolean disabled = Boolean.parseBoolean(Play.configuration.getProperty("hazelcast.disabled", "false"));
    @Override
    public void onApplicationStart() {
        if (disabled) {
            return;
        }
        try {
            if (instance == null) {
                final boolean hazelcastEmbedded = Boolean.parseBoolean(Play.configuration.getProperty("hazelcast.embedded", "true"));
                if (hazelcastEmbedded) {
                    Logger.info("Hazelcast embedded. Change hazelcast.embedded=false to use configFile");
                    createEmbeddedHazelcast();
                } else {
                    final String configFile = Play.configuration.getProperty("hazelcast.configFile", "conf/hazelcast.xml");
                    VirtualFile confXml = Play.getVirtualFile(configFile);
                    if (confXml != null) {
                        Logger.info("Building Hazelcast Configuration for: %s", confXml.getName());
                        XmlConfigBuilder xConf = new XmlConfigBuilder(confXml.inputstream());
                        Config config = xConf.build();
                        config.setInstanceName(Play.configuration.getProperty("application.name", "default"));
                        config.setProperty("hazelcast.jmx", Play.configuration.getProperty("hazelcast.jmx", "true"));
                        config.setProperty("hazelcast.jmx.detailed", Play.configuration.getProperty("hazelcast.jmx.detailed", "true"));
                        config.setProperty("hazelcast.shutdownhook.enabled",
                                Play.configuration.getProperty("hazelcast.shutdownhook.enabled", "true"));
                        try {
                            instance = Hazelcast.getOrCreateHazelcastInstance(config);
                        } catch (IllegalStateException e) {
                            Logger.error("Error creating or finding hazelcast:%s", e);
                            createEmbeddedHazelcast();
                        }
                    } else {
                        Logger.info("Building Hazelcast Configuration using default values...");
                        createEmbeddedHazelcast();
                    }
                }
                Logger.info("Hazelcast Services are now started...\n");
            }
            Logger.info("Configuring HazelcastCacheImpl as default cache...");
            Cache.stop();
            Cache.forcedCacheImpl = HazelcastCacheImpl.getInstance();
            Cache.init();
            Logger.info("Cache Impl: %s", Cache.cacheImpl.getClass().getName());
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
        Injector.inject(this);
        NamedInjector.inject(this);
    }
    private void createEmbeddedHazelcast() {
        Config config = new Config();
        config.setInstanceName(Play.configuration.getProperty("application.name", "default"));
        config.setProperty("hazelcast.jmx", Play.configuration.getProperty("hazelcast.jmx", "true"));
        config.setProperty("hazelcast.jmx.detailed", Play.configuration.getProperty("hazelcast.jmx.detailed", "true"));
        config.setProperty("hazelcast.shutdownhook.enabled", Play.configuration.getProperty("hazelcast.shutdownhook.enabled", "true"));
        instance = Hazelcast.newHazelcastInstance(config);
    }
    @Override
    public void onApplicationStop() {
        if (disabled) {
            return;
        }
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
        if (disabled) {
            return;
        }
        Router.prependRoute("GET", "/@hazel", "HazelcastApplication.index");
    }
    /*
     * (non-Javadoc)
     *
     * @see play.inject.BeanSource#getBeanOfType(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T getBeanOfType(Class<T> clazz) {
        if (disabled) {
            throw new RuntimeException("HazelcastPlugin is disabled!");
        }
        if (clazz.equals(HazelcastInstance.class)) {
            Logger.info("%s Injection...OK", clazz.getName());
            return (T) instance;
        } else if (clazz.equals(ExecutorService.class)) {
            return (T) instance.getExecutorService("defaultPlayExecutorService");
        } else if (clazz.equals(PartitionService.class)) {
            return (T) instance.getPartitionService();
        } else {
            return null;
        }
    }
    /* (non-Javadoc)
     * @see play.inject.NamedBeanSource#getBeanOfType(java.lang.Class, java.lang.String)
     */
    @Override
    public <T> T getBeanOfType(Class<T> clazz, String name) {
        if (disabled) {
            throw new RuntimeException("HazelcastPlugin is disabled!");
        }
        if (clazz.equals(IMap.class) || clazz.equals(Map.class)) {
            return (T) instance.getMap(name);
        } else if (clazz.equals(IList.class) || clazz.equals(List.class)) {
            return (T) instance.getList(name);
        } else if (clazz.equals(ExecutorService.class)) {
            return (T) instance.getExecutorService(name);
        } else if (clazz.equals(ISet.class) || clazz.equals(Set.class)) {
            return (T) instance.getSet(name);
        } else if (clazz.equals(IdGenerator.class)) {
            return (T) instance.getIdGenerator(name);
        } else if (clazz.equals(AtomicLong.class)) {
            return (T) instance.getAtomicLong(name);
        } else if (clazz.equals(MultiMap.class)) {
            return (T) instance.getMultiMap(name);
        } else if (clazz.equals(IQueue.class) || clazz.equals(Queue.class)) {
            return (T) instance.getQueue(name);
        } else if (clazz.equals(ITopic.class)) {
            return (T) instance.getTopic(name);
        } else {
            return null;
        }
    }
    public static HazelcastInstance getHazel() {
        if (disabled) {
            throw new RuntimeException("HazelcastPlugin is disabled!");
        }
        return instance;
    }
    public static TransactionContext getTransactionContext() {
        if (disabled) {
            throw new RuntimeException("HazelcastPlugin is disabled!");
        }
        TransactionOptions options = new TransactionOptions().setTransactionType(TransactionOptions.TransactionType.ONE_PHASE);
        return instance.newTransactionContext(options);
    }
    public static ILock getLock(String name) {
        if (disabled) {
            throw new RuntimeException("HazelcastPlugin is disabled!");
        }
        return instance.getLock(name);
    }
    public static ITopic getTopic(String name) {
        if (disabled) {
            throw new RuntimeException("HazelcastPlugin is disabled!");
        }
        return instance.getTopic(name);
    }
    public static <T> ITopic<T> getTopic(String name, Class<T> cls) {
        if (disabled) {
            throw new RuntimeException("HazelcastPlugin is disabled!");
        }
        return instance.getTopic(name);
    }
    public static IMap getMap(String name) {
        if (disabled) {
            throw new RuntimeException("HazelcastPlugin is disabled!");
        }
        return instance.getMap(name);
    }
    public static <K, T> IMap<K, T> getMap(String name, Class<K> keyType, Class<T> valueType) {
        if (disabled) {
            throw new RuntimeException("HazelcastPlugin is disabled!");
        }
        return instance.getMap(name);
    }
}
