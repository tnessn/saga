package com.github.tnessn.saga.model;

import java.util.Date;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "tx_event")
@CompoundIndexes({
    //name：索引名称 def：字段(1正序 -1倒序) unique：是否唯一索引
	//直接加到字段上面没用
    @CompoundIndex(name = "uq_globalTxId_localTxId", def = "{globalTxId:1, localTxId:1}", unique = true)
})
public class TxEvent {

	  //标记id字段
    @Id
    private ObjectId id;
    
    private String serviceName;
    
    private String globalTxId;
    
    private String localTxId;
    
    private String ip;
    
    private String compensationMethod;
    
    private byte[] payload;
    
    /**
     * 是否已经补偿
     */
    private boolean compensated;
    
    private Date createTime;
    
    private Date updateTime;

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getGlobalTxId() {
		return globalTxId;
	}

	public void setGlobalTxId(String globalTxId) {
		this.globalTxId = globalTxId;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getCompensationMethod() {
		return compensationMethod;
	}

	public void setCompensationMethod(String compensationMethod) {
		this.compensationMethod = compensationMethod;
	}

	public boolean isCompensated() {
		return compensated;
	}

	public void setCompensated(boolean compensated) {
		this.compensated = compensated;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public byte[] getPayload() {
		return payload;
	}

	public void setPayload(byte[] payload) {
		this.payload = payload;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public String getLocalTxId() {
		return localTxId;
	}

	public void setLocalTxId(String localTxId) {
		this.localTxId = localTxId;
	}
}
