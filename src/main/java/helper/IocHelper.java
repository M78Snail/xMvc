package helper;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;

import annotation.Inject;
import utils.ReflectionUtil;

public final class IocHelper {

	static {
		Map<Class<?>, Object> beanMap = BeanHelper.getBeanMap();
		if (MapUtils.isNotEmpty(beanMap)) {
			for (Map.Entry<Class<?>, Object> beanEntry : beanMap.entrySet()) {
				Class<?> beanClass = beanEntry.getKey();
				Object beanInstance = beanEntry.getValue();

				Field[] beanFields = beanClass.getDeclaredFields();

				if (ArrayUtils.isNotEmpty(beanFields)) {
					// 判断是否带有 Inject 注解
					for (Field beanField : beanFields) {
						if (beanField.isAnnotationPresent(Inject.class)) {
							Class<?> beanFieldClass = beanField.getType();
							Object beanFieldInstance = beanMap.get(beanFieldClass);
							if (beanFieldInstance != null) {
								// 通过反射初始化BeanField
								ReflectionUtil.setField(beanInstance, beanField, beanFieldInstance);
							}
						}
					}
				}
			}
		}
	}

}
