package com.github.tnessn.saga.dao;
 
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.github.tnessn.saga.model.TxEvent;
 

/**
 * 
 * @author huangjinfeng
 */
@Repository
public class TxEventDao {
 
 
    @Resource(name = "sagaMongoTemplate")
    private MongoTemplate sagaMongoTemplate;
 
    /**
     * 创建对象
     */
    public void save(TxEvent txEvent) {
    	sagaMongoTemplate.save(txEvent);
    }


	/**
	 * @param globalTx
	 * @return
	 */
	public List<TxEvent> findUncompensatedByGlobalTxId(String globalTxId) {
        Query query=new Query(Criteria.where("globalTxId").is(globalTxId).and("compensated").is(false));
        return  sagaMongoTemplate.find(query, TxEvent.class);
	}
	
    /**
     * 更新对象
     */
    public void update(TxEvent txEvent) {
        Query query=new Query(Criteria.where("id").is(txEvent.getId()));
        Update update= new Update().set("compensated", txEvent.isCompensated()).set("updateTime", new Date());
        sagaMongoTemplate.updateFirst(query,update,TxEvent.class);
    }

	public TxEvent findByGlobalTxIdAndLocalTxId(String globalTxId, String localTxId) {
		Query query = new Query(Criteria.where("globalTxId").is(globalTxId).and("localTxId").is(localTxId));
		return sagaMongoTemplate.findOne(query, TxEvent.class);
	}

	public void deleteByGlobalTxId(String... globalTxIds) {
		Query query = new Query(Criteria.where("globalTxId").in(globalTxIds));
		sagaMongoTemplate.remove(query, TxEvent.class);
	}
	
}
