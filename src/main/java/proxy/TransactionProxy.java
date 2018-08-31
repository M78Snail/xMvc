package proxy;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import annotation.Transaction;
import helper.DatabaseHelper;


public class TransactionProxy implements Proxy {

	private static final Logger LOGGER = LoggerFactory.getLogger(TransactionProxy.class);

	private static final ThreadLocal<Boolean> FLAG_HOLDER = new ThreadLocal<Boolean>() {
		@Override
		protected Boolean initialValue() {
			return false;
		};
	};

	@Override
	public Object doProxy(ProxyChain proxyChain) throws Throwable {
		Object result = null;

		boolean flag = FLAG_HOLDER.get();

		Method method = proxyChain.getTargetMethod();
		if (!flag && method.isAnnotationPresent(Transaction.class)) {
			FLAG_HOLDER.set(true);
			try {
				DatabaseHelper.beginTrasaction();
				LOGGER.debug("开启事务");
				result = proxyChain.doProxyChain();
				DatabaseHelper.commitTrasaction();
				LOGGER.debug("提交事务");
			} catch (Exception e) {
				DatabaseHelper.rollbackTrasaction();
				LOGGER.debug("回滚事务");
			}finally{
				FLAG_HOLDER.remove();
			}
		}else{
			result = proxyChain.doProxyChain();
		}
		return result;
	}

}
