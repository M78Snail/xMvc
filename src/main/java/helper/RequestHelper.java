package helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import bean.FormParam;
import bean.Param;
import utils.CodecUtil;
import utils.StreamUtil;

public class RequestHelper {

	public static Param createParam(HttpServletRequest request) throws IOException {
		List<FormParam> formParamList = new ArrayList<FormParam>();
		formParamList.addAll(parseParameterNames(request));
		formParamList.addAll(parseInputStream(request));
		return null;
	}

	private static List<FormParam> parseParameterNames(HttpServletRequest request) {
		// 创建请求参数
		List<FormParam> formParamList = new ArrayList<FormParam>();
		Enumeration<String> paramNames = request.getParameterNames();
		while (paramNames.hasMoreElements()) {
			String fieldName = paramNames.nextElement();
			String[] fieldValues = request.getParameterValues(fieldName);
			if (ArrayUtils.isNotEmpty(fieldValues)) {
				Object fieldValue;
				if (fieldValues.length == 1) {
					fieldValue = fieldValues[0];
				} else {
					StringBuilder sb = new StringBuilder("");
					for (int i = 0; i < fieldValues.length; i++) {
						sb.append(fieldValues[i]);
						if (i != fieldValues.length - 1) {
							sb.append(Param.SEPARATOR);
						}
					}
					fieldValue = sb.toString();
				}

				formParamList.add(new FormParam(fieldName, fieldValue));
			}
		}
		return formParamList;
	}

	private static List<FormParam> parseInputStream(HttpServletRequest request) throws IOException {
		// 创建请求参数
		List<FormParam> formParamList = new ArrayList<FormParam>();
		String body = CodecUtil.decodeURL(StreamUtil.getString(request.getInputStream()));
		if (StringUtils.isNotEmpty(body)) {
			String[] params = StringUtils.split(body, "&");
			if (ArrayUtils.isNotEmpty(params)) {
				for (String param : params) {
					String[] array = StringUtils.split(param, "=");
					if (ArrayUtils.isNotEmpty(array) && array.length == 2) {
						String paramName = array[0];
						String paramValue = array[1];
						formParamList.add(new FormParam(paramName, paramValue));
					}
				}
			}
		}
		return formParamList;
	}
}
