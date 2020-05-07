package com.github.tnessn.saga.config;

import java.lang.invoke.MethodHandles;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.env.Environment;
import org.springframework.util.ReflectionUtils;

class CompensableAnnotationProcessor implements BeanPostProcessor {
	
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final CallbackContext compensationContext;
	
    @Autowired
    private Environment environment;

	CompensableAnnotationProcessor(CallbackContext compensationContext) {
		this.compensationContext = compensationContext;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        String property = environment.getProperty("spring.application.name");
        if(StringUtils.isBlank(property)) {
        	LOG.error("未配置spring.application.name属性");
        	System.exit(1);
        }
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		ReflectionUtils.doWithMethods(bean.getClass(), new CompensableMethodCheckingCallback(bean, compensationContext));
		return bean;
	}

}
