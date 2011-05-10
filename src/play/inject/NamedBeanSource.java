package play.inject;
import play.inject.BeanSource;


public interface NamedBeanSource {

    public <T> T getBeanOfType(Class<T> clazz, String name);
 
}
