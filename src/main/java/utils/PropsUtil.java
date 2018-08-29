package utils;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PropsUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(PropsUtil.class);

	public static Properties loadProps(String fileName) {
		Properties props = null;
		InputStream is = null;
		try {
			is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
			if (is == null) {
				throw new FileNotFoundException(fileName + "is not find");
			}
			props = new Properties();
			props.load(is);
		} catch (Exception e) {
			LOGGER.error("load properties file failuer");
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (Exception e2) {
					LOGGER.error("close input stream failure", e2);
				}
			}
		}
		return props;
	}

	public static String getString(Properties props, String key) {

		return getString(props, key, "");
	}

	public static String getString(Properties props, String key, String defaultValue) {
		String value = defaultValue;
		if (props.containsKey(key)) {
			value = props.getProperty(key);
		}
		return value;
	}
	
	public static int getInt(Properties props, String key) {

		return getInt(props, key, 0);
	}

	public static int getInt(Properties props, String key, int defaultValue) {
		int value = defaultValue;
		if (props.containsKey(key)) {
			value = Integer.parseInt(props.getProperty(key));
		}
		return value;
	}
	
	public static boolean getBoolean(Properties props, String key) {

		return getBoolean(props, key, false);
	}

	public static boolean getBoolean(Properties props, String key, Boolean defaultValue) {
		boolean value = defaultValue;
		if (props.containsKey(key)) {
			value = Boolean.parseBoolean(props.getProperty(key));
		}
		return value;
	}

}
