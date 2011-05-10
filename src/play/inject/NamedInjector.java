package play.inject;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

import javax.inject.Named;

import org.apache.commons.lang.Validate;

import play.Play;
import play.classloading.enhancers.ControllersEnhancer.ControllerSupport;
import play.jobs.Job;
import play.mvc.Mailer;

public class NamedInjector {
    /**
     * For now, inject beans in controllers
     */
    public static void inject(NamedBeanSource source) {
        List<Class> classes = Play.classloader.getAssignableClasses(ControllerSupport.class);
        classes.addAll(Play.classloader.getAssignableClasses(Mailer.class));
        classes.addAll(Play.classloader.getAssignableClasses(Job.class));
        for(Class<?> clazz : classes) {
            for(Field field : clazz.getDeclaredFields()) {
                if(Modifier.isStatic(field.getModifiers()) && field.isAnnotationPresent(Named.class)) {
                    Class<?> type = field.getType();
                    field.setAccessible(true);
                    try {
                    	Named named = field.getAnnotation(Named.class);
                    	if(named.value() != null || named.value().trim().length() > 0){
                            field.set(null, source.getBeanOfType(type, named.value()));
                    	}
                    } catch(RuntimeException e) {
                        throw e;
                    } catch(Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

}
