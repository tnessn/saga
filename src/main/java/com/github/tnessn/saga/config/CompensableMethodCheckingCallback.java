package com.github.tnessn.saga.config;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.AopProxy;
import org.springframework.aop.support.AopUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;

import com.github.tnessn.saga.aop.Compensable;

/**
 * 
 * @author huangjinfeng
 */
class CompensableMethodCheckingCallback implements MethodCallback {

	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final Object bean;

	private final CallbackContext callbackContext;

	public CompensableMethodCheckingCallback(Object bean, CallbackContext callbackContext) {
		this.bean = bean;
		this.callbackContext = callbackContext;
	}

	@Override
	public void doWith(Method method) throws IllegalArgumentException {
		if (!method.isAnnotationPresent(Compensable.class)) {
			return;
		}
		Compensable compensable = method.getAnnotation(Compensable.class);
		String compensationMethod = compensable.compensationMethod();
		
		if(StringUtils.isBlank(compensationMethod)) {
			return;
		}

		loadMethodContext(method, compensationMethod);
	}

	protected void loadMethodContext(Method method, String... candidates) {
		for (String each : candidates) {
			try {
				Method signature = bean.getClass().getDeclaredMethod(each, method.getParameterTypes());
				String key = getTargetBean(bean).getClass().getDeclaredMethod(each, method.getParameterTypes()).toString();
				callbackContext.addCallbackContext(key, signature, bean);
				LOG.debug("Found callback method [{}] in {}", each, bean.getClass().getCanonicalName());
			} catch (Exception ex) {
				throw new SagaException("No such  Compensation  method [" + each + "] found in " + bean.getClass().getCanonicalName(), ex);
			}
		}
	}

	private Object getTargetBean(Object proxy) throws Exception {
		if (!AopUtils.isAopProxy(proxy)) {
			return proxy;
		}

		if (AopUtils.isJdkDynamicProxy(proxy)) {
			return getJdkDynamicProxyTargetObject(proxy);
		} else {
			return getCglibProxyTargetObject(proxy);
		}
	}

	private Object getCglibProxyTargetObject(Object proxy) throws Exception {
		Field h = proxy.getClass().getDeclaredField("CGLIB$CALLBACK_0");
		h.setAccessible(true);
		Object dynamicAdvisedInterceptor = h.get(proxy);

		Field advised = dynamicAdvisedInterceptor.getClass().getDeclaredField("advised");
		advised.setAccessible(true);

		Object result = ((AdvisedSupport) advised.get(dynamicAdvisedInterceptor)).getTargetSource().getTarget();
		return result;
	}

	private Object getJdkDynamicProxyTargetObject(Object proxy) throws Exception {
		Field h = proxy.getClass().getSuperclass().getDeclaredField("h");
		h.setAccessible(true);
		AopProxy aopProxy = (AopProxy) h.get(proxy);

		Field advised = aopProxy.getClass().getDeclaredField("advised");
		advised.setAccessible(true);

		Object result = ((AdvisedSupport) advised.get(aopProxy)).getTargetSource().getTarget();
		return result;
	}
}
