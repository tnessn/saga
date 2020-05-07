package com.github.tnessn.saga.dao;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.github.tnessn.saga.model.MongoLock;

/**
 * 
 * @author huangjinfeng
 */
@Repository
public class MongoLockDao {

	@Resource(name = "sagaMongoTemplate")
	private MongoTemplate sagaMongoTemplate;

	public List<MongoLock> getByKey(String key) {
		Query query = new Query();
		query.addCriteria(Criteria.where("key").is(key));
		return sagaMongoTemplate.find(query, MongoLock.class);
	}

	public MongoLock incrByWithExpire(String key, double increment, long expire) {
		// 筛选
		Query query = new Query();
		query.addCriteria(new Criteria("key").is(key));

		// 更新
		Update update = new Update();
		update.inc("value", increment);
		update.set("expire", expire);
		// 可选项
		FindAndModifyOptions options = FindAndModifyOptions.options();
		// 没有则新增
		options.upsert(true);
		// 返回更新后的值
		options.returnNew(true);
		MongoLock mongoLock = sagaMongoTemplate.findAndModify(query, update, options, MongoLock.class);
		return mongoLock;
	}

	public void removeExpire(String key, long expireTime) {
		Query query = new Query();
		query.addCriteria(Criteria.where("key").is(key));
		query.addCriteria(Criteria.where("expire").lt(expireTime));
		sagaMongoTemplate.remove(query, MongoLock.class);
	}

	public void remove(Map<String, Object> condition) {
		Query query = new Query();
		Set<Map.Entry<String, Object>> set = condition.entrySet();
		int flag = 0;
		for (Map.Entry<String, Object> entry : set) {
			query.addCriteria(Criteria.where(entry.getKey()).is(entry.getValue()));
			flag = flag + 1;
		}
		if (flag == 0) {
			query = null;
		}
		sagaMongoTemplate.remove(query, MongoLock.class);
	}
}