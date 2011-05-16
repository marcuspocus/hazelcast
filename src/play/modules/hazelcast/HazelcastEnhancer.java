package play.modules.hazelcast;

import java.lang.annotation.Annotation;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import play.Logger;
import play.classloading.ApplicationClasses.ApplicationClass;
import play.classloading.enhancers.Enhancer;

public class HazelcastEnhancer extends Enhancer {

	@Override
	public void enhanceThisClass(ApplicationClass appClass) throws Exception {
		Logger.debug("Check class before enhancement: %s", appClass.name);
		CtClass ctClass = makeClass(appClass);

		// Find an enhance annotated methods.
		for (CtMethod ctMethod : ctClass.getDeclaredMethods()) {
			if (hasAnnotation(ctMethod, HazelTransaction.class.getName())) {
				Logger.info("Enhancing class: %s method: %s...", appClass.name, ctMethod.getName());

				ctMethod.instrument(new ExprEditor(){
					@Override
					public void edit(MethodCall m) throws CannotCompileException {
						m.replace("System.out.println(\"Before\");" +
								"com.hazelcast.core.Transaction _hazelcastTransaction = play.modules.hazelcast.HazelcastPlugin.getHazel().getTransaction();" +
								"if(_hazelcastTransaction.getStatus() != _hazelcastTransaction.TXN_STATUS_ACTIVE)_hazelcastTransaction.begin();" +
								"$_ = $proceed($$);" +
								"if(_hazelcastTransaction.getStatus() == _hazelcastTransaction.TXN_STATUS_ACTIVE)_hazelcastTransaction.commit();"
								 + "System.out.println(\"After\");"
								);
					}
				});
				
				
/*				ctMethod.insertBefore("System.out.println(\"Before\");" +
						"com.hazelcast.core.Transaction _hazelcastTransaction = play.modules.hazelcast.HazelcastPlugin.getHazel().getTransaction();" +
						"_hazelcastTransaction.begin();");
				ctMethod.insertAfter("System.out.println(\"After\");" +
						"_hazelcastTransaction.commit();");
*/				

/*				ctMethod.instrument(new ExprEditor(){
					@Override
					public void edit(MethodCall m) throws CannotCompileException {
						m.replace("System.out.println(\"Before\");" +
								"com.hazelcast.core.Transaction _hazelcastTransaction = play.modules.hazelcast.HazelcastPlugin.getHazel().getTransaction();" +
								"_hazelcastTransaction.begin();" +
								"$_ = $proceed($$);" +
								"System.out.println(\"After\");" +
								"_hazelcastTransaction.commit();" +
								"System.out.println(\"End\");");
					}
				});
*/				
				Logger.debug("Enhancing class: %s method: %s...OK", appClass.name, ctMethod.getName());
			}
		}

		// Done - update class.
		appClass.enhancedByteCode = ctClass.toBytecode();
		ctClass.defrost();
		Logger.debug("Done with this class: %s", appClass.name);
	}

	private boolean hasAnnotation(CtMethod ctMethod, String name) throws ClassNotFoundException {
		Object[] annotations = ctMethod.getAnnotations();
		for (Object o : annotations) {
			Annotation a = (Annotation) o;
			if (a.annotationType().equals(Class.forName(name))) {
					Logger.info("Found Annotation: %s", a.toString());
					return true;
			}
		}
		return false;
	}

}
