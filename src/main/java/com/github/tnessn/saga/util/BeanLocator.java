package com.github.tnessn.saga.util;

import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

/**
 * @author huangjinfeng
 */
@Service("sagaBeanLocator")
public class BeanLocator implements ApplicationContextAware {

    private static ApplicationContext ctx;     //Spring应用上下文环境

    /**
     * 实现ApplicationContextAware接口的回调方法，设置上下文环境
     *
     * @param ctx
     * @throws BeansException
     */
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        BeanLocator.ctx = ctx;
    }


    @SuppressWarnings("unchecked")
	public static  <T> T  getBean(String name) {
        assertContextInjected();
        return  (T)ctx.getBean(name);
    }
    
    
    public static  <T> Map<String, T> getBeansOfType( Class<T> type){
        assertContextInjected();
        return   ctx.getBeansOfType(type);
    }
  
    
    public static  <T> T  getBean(Class<T> clazz) {
        assertContextInjected();
        return  ctx.getBean(clazz);
    }
    
    
    public static <T>String[]  getBeanNames(Class<T> clazz) {
    	 assertContextInjected();
         return  ctx.getBeanNamesForType(clazz);
    }
    
    
    /**
     * 检查ApplicationContext不为空.
     */
    private static void assertContextInjected() {
        if (ctx == null) {
            throw new IllegalStateException("Spring application context未注入,请在spring配置文件中定义BeanLocator");
        }
    }
}