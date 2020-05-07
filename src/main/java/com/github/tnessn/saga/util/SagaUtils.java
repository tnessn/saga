package com.github.tnessn.saga.util;

/**
 * 生成分布式事务全局id
 * @author huangjinfeng
 */
public class SagaUtils {
	
	private static ThreadLocal<String> globalTxId=new ThreadLocal<String>();

	public static String getGlobalTxId() {
		return globalTxId.get();
	}

	public static void setGlobalTxId(String globalTxId) {
		SagaUtils.globalTxId.set(globalTxId);
	}
}

