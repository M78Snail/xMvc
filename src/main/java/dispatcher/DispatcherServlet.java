package dispatcher;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import bean.Data;
import bean.Handler;
import bean.Param;
import bean.View;
import helper.BeanHelper;
import helper.ConfigHelper;
import helper.ControllerHelper;
import helper.RequestHelper;
import helper.UploadHelper;
import loader.HelperLoader;
import utils.JsonUtil;
import utils.ReflectionUtil;

@WebServlet(urlPatterns = "/*", loadOnStartup = 0)
public class DispatcherServlet extends HttpServlet {

	private static final long serialVersionUID = -4541844387836086509L;

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
		UploadHelper.init(servletContext);
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

			Param param = null;

			if (UploadHelper.isMultipart(req)) {
				param = UploadHelper.createParam(req);
			} else {
				param = RequestHelper.createParam(req);
			}

			Object result;

			// 反射 调用Action方法
			Method actionMethod = handler.getActionMethod();

			if (param.isEmpty()) {
				result = ReflectionUtil.invokeMethod(controllerBean, actionMethod);
			} else {
				result = ReflectionUtil.invokeMethod(controllerBean, actionMethod, param);
			}

			// 处理Action返回
			if (result instanceof View) {
				View view = (View) result;
				handleViewResult(view, req, resp);
			} else if (result instanceof Data) {
				Data data = (Data) result;
				handleDataResult(data, resp);
			}
		} else {
			System.out.println("未找到Handler");
		}
	}

	private void handleViewResult(View view, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		String path = view.getPath();
		if (StringUtils.isNotEmpty(path)) {
			if (path.startsWith("/")) {
				response.sendRedirect(request.getContextPath() + path);
			} else {
				Map<String, Object> model = view.getModel();
				for (Map.Entry<String, Object> entry : model.entrySet()) {
					request.setAttribute(entry.getKey(), entry.getValue());
				}
				request.getRequestDispatcher(ConfigHelper.getAppJspPath() + path).forward(request, response);
			}
		}
	}

	private void handleDataResult(Data data, HttpServletResponse response) throws IOException {
		Object model = data.getModel();
		if (model != null) {
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			PrintWriter writer = response.getWriter();
			String json = JsonUtil.toJson(model);
			writer.write(json);
			writer.flush();
			writer.close();
		}
	}

}
