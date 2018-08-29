package dispatcher;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import bean.Data;
import bean.Handler;
import bean.Param;
import bean.View;
import helper.BeanHelper;
import helper.ConfigHelper;
import helper.ControllerHelper;
import loader.HelperLoader;
import utils.CodecUtil;
import utils.JsonUtil;
import utils.ReflectionUtil;
import utils.StreamUtil;

@WebServlet(urlPatterns = "/*", loadOnStartup = 0)
public class DispatcherServlet extends HttpServlet {

	@Override
	public void init(ServletConfig config) throws ServletException {
		// 初始化相关Helper类
		HelperLoader.init();

		// 注册Servlet
		ServletContext servletContext = config.getServletContext();
		// 注册处理JSP的Servlet
		ServletRegistration jspServlet = servletContext.getServletRegistration("jsp");
		jspServlet.addMapping(ConfigHelper.getAppJspPath() + "*");

		// 注册处理静态资源的Servlet
		ServletRegistration defaultServlet = servletContext.getServletRegistration("default");
		defaultServlet.addMapping(ConfigHelper.getAppAssetPath() + "*");
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 获取请求方法和路径
		String requestMethod = req.getMethod().toLowerCase();
		String requestPath = req.getPathInfo();

		// 获取Action处理器
		Handler handler = ControllerHelper.getHandler(requestMethod, requestPath);
		if (handler != null) {
			Class<?> controllerClass = handler.getControllerClass();
			System.out.println("找到Handler>>>" + controllerClass.getSimpleName());
			Object controllerBean = BeanHelper.getBean(controllerClass);
			// 创建请求参数
			Map<String, Object> paramMap = new HashedMap<String, Object>();
			Enumeration<String> paramNames = req.getParameterNames();
			while (paramNames.hasMoreElements()) {
				String paramName = paramNames.nextElement();
				String paramValue = req.getParameter(paramName);
				paramMap.put(paramName, paramValue);
			}
			String body = CodecUtil.decodeURL(StreamUtil.getString(req.getInputStream()));
			if (StringUtils.isNotEmpty(body)) {
				String[] params = StringUtils.split(body, "&");
				if (ArrayUtils.isNotEmpty(params)) {
					for (String param : params) {
						String[] array = StringUtils.split(param, "=");
						if (ArrayUtils.isNotEmpty(array) && array.length == 2) {
							String paramName = array[0];
							String paramValue = array[1];
							paramMap.put(paramName, paramValue);
						}
					}
				}
			}

			Param param = new Param(paramMap);
			// 反射 调用Action方法
			Method actionMethod = handler.getActionMethod();

			Object result = ReflectionUtil.invokeMethod(controllerBean, actionMethod, param);

			// 处理Action返回
			if (result instanceof View) {
				View view = (View) result;
				String path = view.getPath();
				if (StringUtils.isNotEmpty(path)) {
					if (path.startsWith("/")) {
						resp.sendRedirect(req.getContextPath() + path);
					} else {
						Map<String, Object> model = view.getModel();
						for (Map.Entry<String, Object> entry : model.entrySet()) {
							req.setAttribute(entry.getKey(), entry.getValue());
						}
						req.getRequestDispatcher(ConfigHelper.getAppJspPath() + path).forward(req, resp);
					}
				}
			} else if (result instanceof Data) {
				Data data = (Data) result;
				Object model = data.getModel();
				if (model != null) {
					resp.setContentType("application/json");
					resp.setCharacterEncoding("UTF-8");
					PrintWriter writer = resp.getWriter();
					String json = JsonUtil.toJson(model);
					writer.write(json);
					writer.flush();
					writer.close();
				}
			}
		} else {
			System.out.println("未找到Handler");
		}
	}

}
