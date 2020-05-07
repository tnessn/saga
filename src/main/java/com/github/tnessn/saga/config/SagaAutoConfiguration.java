package com.github.tnessn.saga.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import com.mongodb.MongoClientURI;

/**
 * 描述：配置类
 *
 * @Author huangjinfeng
 **/
@Configuration
@EnableConfigurationProperties(SagaProperties.class)
@ComponentScan(basePackages = {"com.github.tnessn.saga"})
public class SagaAutoConfiguration {


	@Bean(name = "sagaMongoDbFactory")
	public MongoDbFactory dbFactory(SagaProperties sagaProperties) {
		return new SimpleMongoDbFactory(new MongoClientURI(sagaProperties.getMongodbUri()));
	}

	@Bean(name = "sagaMongoTemplate")
	public MongoTemplate mongoTemplate( @Qualifier("sagaMongoDbFactory") MongoDbFactory dbFactory) {
		return new MongoTemplate(dbFactory);
	}

	@Bean
	public CompensableAnnotationProcessor compensableAnnotationProcessor(CallbackContext compensationContext) {
		return new CompensableAnnotationProcessor(compensationContext);
	}

	@Bean(name = { "compensationContext" })
	public CallbackContext compensationContext() {
		return new CallbackContext();
	}

}