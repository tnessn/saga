package com.github.tnessn.saga.aop;

import java.lang.reflect.Method;

import javax.annotation.Resource;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import com.github.tnessn.saga.config.SagaException;
import com.github.tnessn.saga.dao.TxEventDao;
import com.github.tnessn.saga.model.DistrictedTransationProperties;
import com.github.tnessn.saga.model.TxEvent;

/**
 * @author huangjinfeng
 *
 */
@Service
@Aspect
@Order(1)
public class DtIdempotentAspceJAdvice {
	
	private static final Logger logger = LoggerFactory.getLogger(DtIdempotentAspceJAdvice.class);
	
	@Resource
	private TxEventDao txEventDao;
	
	
	@Pointcut("@annotation(com.github.tnessn.saga.aop.DtIdempotent)")
	public void pointcut() {};
	
	
	@Around(value = "pointcut()")
	public Object  around(ProceedingJoinPoint  joinPoint) throws Throwable {
		logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> {}.{}  开始幂等校验",joinPoint.getSignature().getDeclaringTypeName() , joinPoint.getSignature().getName() );
		Object[] args = joinPoint.getArgs();
		DistrictedTransationProperties d = null;
		for (int i = 0; i < args.length; i++) {
			try {
				d = (DistrictedTransationProperties) args[i];
				break;
			} catch (Exception e) {
			}
		}

		if (d == null) {
			throw new SagaException("参数必须继承DistrictedTransationProperties接口");
		}
		
		//幂等判断
		TxEvent txEvent=txEventDao.findByGlobalTxIdAndLocalTxId(d.getGlobalTxId(),d.getLocalTxId());
		
		Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
		if(method.isAnnotationPresent(Compensable.class)) {
			if(txEvent!=null) {
				logger.error("{}已经执行过，globalTxId={},localTxId={}",method.toString(),d.getGlobalTxId(),d.getLocalTxId());
				return null;
			}
		}else {
			if(txEvent!=null&&txEvent.isCompensated()) {
				logger.error("{}已经执行过，globalTxId={},localTxId={}",method.toString(),d.getGlobalTxId(),d.getLocalTxId());
				return null;
			}
		}
		return joinPoint.proceed();
		
	}
	
}