package helper;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.lang3.ArrayUtils;

import annotation.Action;
import bean.Handler;
import bean.Request;





public final class ControllerHelper {

	private static final Map<Request, Handler> ACTION_MAP = new HashedMap<Request, Handler>();

	static {
		Set<Class<?>> controllerClassSet = ClassHelper.getControllerClassSet();
		if (CollectionUtils.isNotEmpty(controllerClassSet)) {
			for (Class<?> controllerClass : controllerClassSet) {
				// 获取定义的方法
				Method[] methods = controllerClass.getDeclaredMethods();
				if (ArrayUtils.isNotEmpty(methods)) {
					for (Method method : methods) {
						if (method.isAnnotationPresent(Action.class)) {
							// 从Action注解获取URL映射规则
							Action action = method.getAnnotation(Action.class);
							String mapping = action.value();
							// 验证url映射规则
							if (mapping.matches("\\w+:/\\w*")) {
								String[] array = mapping.split(":");
								if (ArrayUtils.isNotEmpty(array) && array.length == 2) {
									// 获取请求方法
									String requestMethod = array[0];
									// 获取请求路径
									String requestPath = array[1];

									Request request = new Request(requestMethod, requestPath);
									Handler handler = new Handler(controllerClass, method);
									ACTION_MAP.put(request, handler);
								}
							}
						}
					}
				}
			}
		}
	}

	public static Handler getHandler(String requestMethod, String requestPath) {
		Request request = new Request(requestMethod, requestPath);
		return ACTION_MAP.get(request);
	}
}
