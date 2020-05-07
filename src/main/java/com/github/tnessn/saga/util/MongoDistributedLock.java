package com.github.tnessn.saga.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.tnessn.saga.dao.MongoLockDao;
import com.github.tnessn.saga.model.MongoLock;

/**
 * 
 * @author huangjinfeng
 */
public class MongoDistributedLock {

	static MongoLockDao mongoLockDao;

	static {
		mongoLockDao = BeanLocator.getBean(MongoLockDao.class);
	}

	public static boolean tryLock(String key, long expire) {
		List<MongoLock> mongoLocks = mongoLockDao.getByKey(key);
		// 判断该锁是否被获得,锁已经被其他请求获得，直接返回
		if (mongoLocks.size() > 0 && mongoLocks.get(0).getExpire() >= System.currentTimeMillis()) {
			return false;
		}
		// 释放过期的锁
		if (mongoLocks.size() > 0 && mongoLocks.get(0).getExpire() < System.currentTimeMillis()) {
			releaseLockExpire(key, System.currentTimeMillis());
		}
		// ！！(在高并发前提下)在当前请求已经获得锁的前提下，还可能有其他请求尝试去获得锁，此时会导致当前锁的过期时间被延长，由于延长时间在毫秒级，可以忽略。
		MongoLock mapResult;
		try {
			mapResult = mongoLockDao.incrByWithExpire(key, 1, System.currentTimeMillis() + expire);
		} catch (Exception e) {
			return false;
		}
		// 如果结果是1，代表当前请求获得锁
		if (mapResult.getValue() == 1) {
			return true;
			// 如果结果>1，表示当前请求在获取锁的过程中，锁已被其他请求获得。
		} else if (mapResult.getValue() > 1) {
			return false;
		}
		return false;
	}

	public static void releaseLock(String key) {
		Map<String, Object> condition = new HashMap<>();
		condition.put("key", key);
		mongoLockDao.remove(condition);
	}

	private static void releaseLockExpire(String key, long expireTime) {
		mongoLockDao.removeExpire(key, expireTime);
	}

	public static void tryLock(String key, long expireTime, LockCall lockCall) {
		boolean lock = tryLock(key, expireTime);
		if (lock) {
			try {
				lockCall.accept();
			} finally {
				releaseLock(key);
			}
		}
	}

	public interface LockCall {
		void accept();
	}
}