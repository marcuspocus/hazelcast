package play.modules.hazelcast;
import play.inject.BeanSource;


public interface NamedBeanSource {

    <T> T getBeanOfType(Class<T> clazz, String name);
 
}
