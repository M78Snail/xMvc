# xMvc
# 一款简单的Mvc框架

# 配置：

1. 在resources目录下，创建simple.properties (名称不可变)

   ```java
   simple.framework.jdbc.driver=com.mysql.jdbc.Driver
   simple.framework.jdbc.url=jdbc:mysql://localhost:3306/day06
   simple.framework.jdbc.username=root
   simple.framework.jdbc.password=root
   
   simple.framework.app.base_package=com.zjipst.simple_framework
   simple.framework.app.jsp_path=/WEB-INF/view/
   simple.framework.app.asset_path=/asset/
   ```

2. Java根目录需和 simple.framework.app.base_package 值相同

3. 将jar包放入pom目录中，并添加配置

   ```java
   <dependency>
   	<groupId>com.zjipst</groupId>
   	<artifactId>xMvc</artifactId>
   	<version>0.0.1-SNAPSHOT</version>
   </dependency>
   ```




# 使用：

```java
@Controller
public class TestController {

	@Inject
	private TestService testService;

	@Action("get:/test")
	public Data test(Param param) {
		long id = param.getLong("id");

		List<User> userList = testService.getUserList();
		System.out.println("userList>>" + userList);

		return new Data(200);
	}

	@Action("post:/test_file")
	public Data testFile(Param param) {
		Map<String, Object> fieldMap = param.getFieldMap();
		FileParam fileParam = param.getFile("photo");
		testService.testUploadFile(fieldMap, fileParam);
		return new Data(200);

	}

}
```

------

### <1> Mvc

1. 类名上加 **@Controller** 注解

2. 方法上加 **@Action** 注解

   - 冒号前表明请求类型；
   - 冒号后表明请求路径；

3. 参数

   - 通过Param 获取所有参数
     - Map<String, Object> fieldMap = param.getFieldMap();
   - 文件上传
     - FileParam fileParam = param.getFile("photo"); 通过FileParam 获取文件
     - UploadHelper.uploadFile("D://cache//", fileParam);  上传文件

4. 返回

   - 返回 View 路径

     ```java
     return new View("login").addModel("user","name");
     ```

   - 返回 Json 数据

     ```
     return new Data(200);
     ```

------

### <2> ICO

通过 @Inject 注解 实现ICO 控制反转

```
@Inject
private TestService testService;
```

------

### <3> AOP

```java
@Aop(Controller.class)
public class ControllerAop extends AopProxy {

	@Override
	public void before(Class<?> cls, Method method, Object[] params) throws Throwable {
		System.out.println("收到请求>>>" + method.getName());
	}

}
```

1. 在切面类上，加入 @Aop 注解并需继承 AopProxy类；
2. @Aop的 Value 表明针对哪个注解下的类的所有方法进行切面；
3. 可用来重写的方法有 **before** ， **after**，**error**，**end** 四个方法

------

### <4> 事务代理

```java
@Service
public class TestService {

	@Transaction
	public List<User> getUserList() {
		return DatabaseHelper.queryEntityList(User.class, "select * from user");
	}

	public boolean testUploadFile(Map<String, Object> fieldMap, FileParam fileParam) {

		System.out.println("fieldMap>>>>" + fieldMap.get("id"));
		UploadHelper.uploadFile("D://cache//", fileParam);
		return true;
	}
}
```

1. **@Service** 注解标识Service类

2. 涉及事务的方法需要加 **@Transaction** 注解

3. 使用 **DatabaseHelper** 进行增删改查

   ```java
   DatabaseHelper.insertEntity(User.class, fieldMap);
   DatabaseHelper.deleteEntity(User.class, 123);
   DatabaseHelper.updateEntity(User.class, 123, fieldMap);
   DatabaseHelper.queryEntityList(User.class, "select * from user");
   ```


