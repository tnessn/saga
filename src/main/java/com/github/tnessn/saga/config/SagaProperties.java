package com.github.tnessn.saga.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 描述：配置信息 实体
 *
 * @Author huangjinfeng
 **/
@ConfigurationProperties(prefix = "saga")
public class SagaProperties {
    private String mongodbUri;
    
	public String getMongodbUri() {
		return mongodbUri;
	}
	public void setMongodbUri(String mongodbUri) {
		this.mongodbUri = mongodbUri;
	}
	
}