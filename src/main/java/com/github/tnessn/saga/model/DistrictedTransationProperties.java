/**
 * 
 */
package com.github.tnessn.saga.model;

import java.io.Serializable;

import com.github.tnessn.saga.util.SagaUtils;
import com.github.tnessn.saga.util.UUIDUtils;


/**
 * @author huangjinfeng
 */
public class DistrictedTransationProperties implements  Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String localTxId=UUIDUtils.get32UUID();   
	private String globalTxId=SagaUtils.getGlobalTxId();
	public String getGlobalTxId() {
		return globalTxId;
	}
	public void setGlobalTxId(String globalTxId) {
		this.globalTxId = globalTxId;
	}
	public String getLocalTxId() {
		return localTxId;
	}
	public void setLocalTxId(String localTxId) {
		this.localTxId = localTxId;
	}


}
