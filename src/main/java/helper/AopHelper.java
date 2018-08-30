package helper;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import annotation.Aop;
import proxy.AopProxy;
import proxy.Proxy;
import proxy.ProxyManager;

public final class AopHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(AopHelper.class);

	static {
		try {
			Map<Class<?>, Set<Class<?>>> proxyMap = createProxyMap();
			Map<Class<?>, List<Proxy>> targetMap = createTargetMap(proxyMap);
			for (Map.Entry<Class<?>, List<Proxy>> targetEntity : targetMap.entrySet()) {
				Class<?> targetClass = targetEntity.getKey();
				List<Proxy> proxyList = targetEntity.getValue();
				Object proxy = ProxyManager.creatProxy(targetClass, proxyList);
				BeanHelper.setBean(targetClass, proxy);
			}
		} catch (Exception e) {
			LOGGER.error("aop failure", e);
		}
	}

	private static Set<Class<?>> createTargetClassSet(Aop aop) throws Exception {
		Set<Class<?>> targetClassSet = new HashSet<Class<?>>();

		Class<? extends Annotation> annotation = aop.value();

		if (annotation != null && !annotation.equals(Aop.class)) {
			targetClassSet.addAll(ClassHelper.getClassSetByAnnotation(annotation));
		}

		return targetClassSet;
	}

	/**
	 * 映射 一个代理类可能对应一个或多个目标类
	 * 
	 * @return
	 * @throws Exception
	 */
	private static Map<Class<?>, Set<Class<?>>> createProxyMap() throws Exception {
		Map<Class<?>, Set<Class<?>>> proxyMap = new HashMap<Class<?>, Set<Class<?>>>();
		Set<Class<?>> proxyClassSet = ClassHelper.getClassSetBySupper(AopProxy.class);
		for (Class<?> proxyClass : proxyClassSet) {
			if (proxyClass.isAnnotationPresent(Aop.class)) {
				Aop aop = proxyClass.getAnnotation(Aop.class);
				Set<Class<?>> targetClassSet = createTargetClassSet(aop);
				proxyMap.put(proxyClass, targetClassSet);
			}
		}
		return proxyMap;
	}

	private static Map<Class<?>, List<Proxy>> createTargetMap(Map<Class<?>, Set<Class<?>>> proxyMap) throws Exception {
		Map<Class<?>, List<Proxy>> targetMap = new HashMap<Class<?>, List<Proxy>>();

		for (Map.Entry<Class<?>, Set<Class<?>>> proxyEntry : proxyMap.entrySet()) {
			Class<?> proxyClass = proxyEntry.getKey();
			Set<Class<?>> targetClassSet = proxyEntry.getValue();

			for (Class<?> targetClass : targetClassSet) {
				Proxy proxy = (Proxy) proxyClass.newInstance();
				if (targetMap.containsKey(targetClass)) {
					targetMap.get(targetClass).add(proxy);
				} else {
					List<Proxy> proxyList = new ArrayList<Proxy>();
					proxyList.add(proxy);
					targetMap.put(targetClass, proxyList);
				}
			}
		}

		return targetMap;
	}

}
