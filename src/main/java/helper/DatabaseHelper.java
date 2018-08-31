package helper;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.PropsUtil;

public class DatabaseHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseHelper.class);

	private static final ThreadLocal<Connection> CONNECTION_HOLDER;

	private static final QueryRunner QUERY_RUNNER;

	private static final BasicDataSource DATA_SOURCE;
	static {
		CONNECTION_HOLDER = new ThreadLocal<Connection>();
		QUERY_RUNNER = new QueryRunner();

		DATA_SOURCE = new BasicDataSource();
		Properties conf = PropsUtil.loadProps(ConfigConstant.CONFIG_FILE);

		DATA_SOURCE.setDriverClassName(conf.getProperty(ConfigConstant.JDBC_DRIVER));
		DATA_SOURCE.setUrl(conf.getProperty(ConfigConstant.JDBC_URL));
		DATA_SOURCE.setUsername(conf.getProperty(ConfigConstant.JDBC_USERNAME));
		DATA_SOURCE.setPassword(conf.getProperty(ConfigConstant.JDBC_PASSWORD));
	}

	private static Connection getConnection() {
		Connection conn = CONNECTION_HOLDER.get();
		if (conn == null) {
			try {
				conn = DATA_SOURCE.getConnection();
			} catch (SQLException e) {
				LOGGER.error("get connection failure", e);
				throw new RuntimeException(e);
			} finally {
				CONNECTION_HOLDER.set(conn);
			}
		}
		return conn;
	}

	/**
	 * 单表查询
	 * 
	 * @param sql
	 * @param params
	 * @return
	 */
	public static <T> List<T> queryEntityList(Class<T> entityClass, String sql, Object... params) {
		List<T> entityList = null;

		try {
			Connection conn = getConnection();
			entityList = QUERY_RUNNER.query(conn, sql, new BeanListHandler<T>(entityClass), params);
		} catch (Exception e) {
			LOGGER.error("execute QueryEntityList failure", e);
			throw new RuntimeException(e);
		}

		return entityList;
	}

	/**
	 * 查询单个
	 * 
	 * @param sql
	 * @param params
	 * @return
	 */
	public static <T> T queryEntity(Class<T> entityClass, String sql, Object... params) {
		T entity;

		try {
			Connection conn = getConnection();
			entity = QUERY_RUNNER.query(conn, sql, new BeanHandler<T>(entityClass), params);
		} catch (Exception e) {
			LOGGER.error("execute QueryEntity failure", e);
			throw new RuntimeException(e);
		}

		return entity;
	}

	/**
	 * 多表查询
	 * 
	 * @param sql
	 * @param params
	 * @return
	 */
	public static List<Map<String, Object>> executeQuery(String sql, Object... params) {
		List<Map<String, Object>> result = null;

		try {
			Connection conn = getConnection();
			result = QUERY_RUNNER.query(conn, sql, new MapListHandler(), params);
		} catch (Exception e) {
			LOGGER.error("execute Query failure", e);
			throw new RuntimeException(e);
		}

		return result;
	}

	/**
	 * 更新通用方法
	 * 
	 * @param sql
	 * @param params
	 * @return
	 */
	public static int executeUpdate(String sql, Object... params) {
		int rows = 0;

		try {
			Connection conn = getConnection();
			rows = QUERY_RUNNER.update(conn, sql, params);
		} catch (Exception e) {
			LOGGER.error("execute Update failure", e);
			throw new RuntimeException(e);
		}

		return rows;
	}

	/**
	 * 插入单个
	 * 
	 * @param sql
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> boolean insertEntity(Class<T> entityClass, Map<String, Object> fieldMap) {
		if (CollectionUtils.isEmpty((Collection<T>) fieldMap)) {
			LOGGER.error("can not insert entity:fieldMap is empty");
			return false;
		}
		String sql = "INSERT INTO" + getTableName(entityClass);
		StringBuilder columns = new StringBuilder("(");
		StringBuilder values = new StringBuilder("(");

		for (String fieldName : fieldMap.keySet()) {
			columns.append(fieldName).append(", ");
			values.append("?, ");
		}
		columns.replace(columns.lastIndexOf(", "), columns.length(), ")");
		values.replace(values.lastIndexOf(", "), values.length(), ")");
		sql += columns + " VALUES " + values;
		Object[] params = fieldMap.values().toArray();
		return executeUpdate(sql, params) == 1;
	}

	/**
	 * 查询单个
	 * 
	 * @param sql
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> boolean updateEntity(Class<T> entityClass, long id, Map<String, Object> fieldMap) {
		if (CollectionUtils.isEmpty((Collection<T>) fieldMap)) {
			LOGGER.error("can not update entity:fieldMap is empty");
			return false;
		}
		String sql = "UPDATE " + getTableName(entityClass) + " SET ";
		StringBuilder columns = new StringBuilder("(");

		for (String fieldName : fieldMap.keySet()) {
			columns.append(fieldName).append("=?, ");

		}
		sql += columns.substring(0, columns.lastIndexOf(", ")) + " WHERE id = ?";

		List<Object> paramList = new ArrayList<Object>();
		paramList.addAll(fieldMap.values());
		paramList.add(id);
		Object[] params = paramList.toArray();
		return executeUpdate(sql, params) == 1;
	}

	public static <T> boolean deleteEntity(Class<T> entityClass, long id) {
		String sql = "DELETE FROM " + getTableName(entityClass) + " WHERE id = ? ";
		return executeUpdate(sql, id) == 1;
	}

	private static String getTableName(Class<?> entityClass) {
		return entityClass.getSimpleName();
	}

	/**
	 * 开启事务
	 */
	public static void beginTrasaction() {
		Connection conn = getConnection();
		if (conn != null) {
			try {
				conn.setAutoCommit(false);
			} catch (Exception e) {
				LOGGER.error("begin Trasaction failure", e);
				throw new RuntimeException(e);
			} finally {
				CONNECTION_HOLDER.set(conn);
			}
		}
	}

	/**
	 * 提交事务
	 */
	public static void commitTrasaction() {
		Connection conn = getConnection();
		if (conn != null) {
			try {
				conn.commit();
				conn.close();
			} catch (Exception e) {
				LOGGER.error("commit Trasaction failure", e);
				throw new RuntimeException(e);
			} finally {
				CONNECTION_HOLDER.remove();
			}
		}
	}

	/**
	 * 回滚事务
	 */
	public static void rollbackTrasaction() {
		Connection conn = getConnection();
		if (conn != null) {
			try {
				conn.rollback();
				conn.close();
			} catch (Exception e) {
				LOGGER.error("rollback Trasaction failure", e);
				throw new RuntimeException(e);
			} finally {
				CONNECTION_HOLDER.remove();
			}
		}
	}

}
