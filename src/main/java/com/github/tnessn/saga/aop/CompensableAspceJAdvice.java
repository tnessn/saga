package com.github.tnessn.saga.aop;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.Date;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.github.tnessn.saga.config.SagaException;
import com.github.tnessn.saga.dao.GlobalTxDao;
import com.github.tnessn.saga.dao.TxEventDao;
import com.github.tnessn.saga.enums.GlobalTxStatusEnum;
import com.github.tnessn.saga.model.DistrictedTransationProperties;
import com.github.tnessn.saga.model.GlobalTx;
import com.github.tnessn.saga.model.TxEvent;
import com.github.tnessn.saga.util.SerializeUtils;

/**
 * @author huangjinfeng
 *
 */
@Service
@Aspect
@Order(2)
public class CompensableAspceJAdvice {

	private static final Logger LOG = LoggerFactory.getLogger(CompensableAspceJAdvice.class);

	@Resource
	private TxEventDao txEventDao;
	@Resource
	private GlobalTxDao globalTxDao;
	@Autowired
	private Environment env;
	
	@Pointcut("@annotation(com.github.tnessn.saga.aop.Compensable)")
	public void pointcut() {
	};
	
	
	@Before(value = "pointcut()&& @annotation(compensable)")
	public void beforeAdvice(JoinPoint joinPoint,Compensable compensable) {
		DistrictedTransationProperties d = getDistrictedTransationProperties(joinPoint);
		if(StringUtils.isBlank(d.getGlobalTxId())) {
			throw new SagaException("调用者必须添加@SagaStart注解");
		}
	}

	@AfterReturning(value = "pointcut()&& @annotation(compensable)", returning = "retVal")
	public void afterReturningAdvice(JoinPoint joinPoint, Compensable compensable, Object retVal) throws Exception {
		DistrictedTransationProperties d = getDistrictedTransationProperties(joinPoint);

		TxEvent txEvent = new TxEvent();
		txEvent.setGlobalTxId(d.getGlobalTxId());
		txEvent.setCreateTime(new Date());
		txEvent.setServiceName(env.getProperty("spring.application.name"));
		String compensationMethod = compensable.compensationMethod();
		if (StringUtils.isNotBlank(compensationMethod)) {
			txEvent.setCompensated(false);
		}
		
		
		txEvent.setIp(InetAddress.getLocalHost().getHostAddress());
		Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
		txEvent.setCompensationMethod(joinPoint.getTarget().getClass().getDeclaredMethod(compensable.compensationMethod(), method.getParameterTypes()).toString());
		txEvent.setPayload(SerializeUtils.serialize(joinPoint.getArgs()));
		txEvent.setLocalTxId(d.getLocalTxId());
		txEventDao.save(txEvent);
	}

	private DistrictedTransationProperties getDistrictedTransationProperties(JoinPoint joinPoint) {
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
		
		return d;
	}

	@AfterThrowing(value = "pointcut()", throwing = "ex")
	public void afterThrowingAdvice(JoinPoint joinPoint, Exception ex) {
		DistrictedTransationProperties d = getDistrictedTransationProperties(joinPoint);
		// 异常做补偿
		GlobalTx globalTx = new GlobalTx();
		globalTx.setId(d.getGlobalTxId());
		globalTx.setUpdateTime(new Date());
		globalTx.setStatus(GlobalTxStatusEnum.ERROR.getCode());
		globalTxDao.update(globalTx);
	}
}