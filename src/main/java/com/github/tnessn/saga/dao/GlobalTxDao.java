package com.github.tnessn.saga.dao;
 
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.github.tnessn.saga.enums.GlobalTxStatusEnum;
import com.github.tnessn.saga.model.GlobalTx;
 

/**
 * 
 * @author huangjinfeng
 */
@Repository
public class GlobalTxDao {
 
 
    @Resource(name = "sagaMongoTemplate")
    private MongoTemplate sagaMongoTemplate;
    
 
    /**
     * 创建对象
     */
    public void save(GlobalTx globalTx) {
    	sagaMongoTemplate.save(globalTx);
    }

	/**
	 * @return
	 */
	public List<GlobalTx> findAll() {
		return sagaMongoTemplate.findAll(GlobalTx.class);
	}
	
	public List<GlobalTx> findAndRemoveCompensatedAndFinish(int limit,int afterMillisecond) {
		Query query=new Query(Criteria.where("status").in(GlobalTxStatusEnum.FINISHED.getCode(),GlobalTxStatusEnum.COMPENSATED.getCode()).and("createTime").lt(DateUtils.addMilliseconds(new Date(), -(int)afterMillisecond)));
		query.limit(limit);
		return sagaMongoTemplate.findAllAndRemove(query,GlobalTx.class);
	}
    
    
    public List<GlobalTx> find(int status,long millisecond,int limit,Direction direction){
    	 Query query=new Query(Criteria.where("status").is(status).and("createTime").lt(DateUtils.addMilliseconds(new Date(), -(int)millisecond)));
    	 query.limit(limit);
    	 query.with(new Sort(direction,"_id"));
         return  sagaMongoTemplate.find(query, GlobalTx.class);
    }
    
	
    /**
     * 更新对象
     */
    public void update(GlobalTx globalTx) {
        Query query=new Query(Criteria.where("id").is(globalTx.getId()));
        Update update= new Update().set("status", globalTx.getStatus()).set("updateTime", new Date());
        sagaMongoTemplate.updateFirst(query,update,GlobalTx.class);
    }
}
