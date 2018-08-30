package utils;

import java.net.URLDecoder;
import java.net.URLEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CodecUtil {
	private static final Logger LOGGER = LoggerFactory.getLogger(CodecUtil.class);

	@SuppressWarnings("deprecation")
	public static String encodeURL(String source) {
		String target;
		try {
			target = URLEncoder.encode(source);
		} catch (Exception e) {
			LOGGER.error("encode url failure", e);
			throw new RuntimeException();
		}
		return target;
	}

	@SuppressWarnings("deprecation")
	public static String decodeURL(String source) {
		String target;
		try {
			target = URLDecoder.decode(source);
		} catch (Exception e) {
			LOGGER.error("decode url failure", e);
			throw new RuntimeException();
		}
		return target;
	}
}
