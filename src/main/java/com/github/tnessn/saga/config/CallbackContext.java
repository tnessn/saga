package com.github.tnessn.saga.config;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CallbackContext {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final Map<String, CallbackContextInternal> contexts = new ConcurrentHashMap<>();


  public void addCallbackContext(String key, Method compensationMethod, Object target) {
    compensationMethod.setAccessible(true);
    contexts.put(key, new CallbackContextInternal(target, compensationMethod));
  }
  
  public void apply(String callbackMethod, Object... payloads) {
	    CallbackContextInternal contextInternal = contexts.get(callbackMethod);
	    try {
	      contextInternal.callbackMethod.invoke(contextInternal.target, payloads);
	    } catch (IllegalAccessException | InvocationTargetException e) {
	    	throw new SagaException("执行补偿失败,补偿方法:"+callbackMethod)  ;
	  }
  }


  private static final class CallbackContextInternal {
    private final Object target;

    private final Method callbackMethod;

    private CallbackContextInternal(Object target, Method callbackMethod) {
      this.target = target;
      this.callbackMethod = callbackMethod;
    }
  }
}
