package com.github.tnessn.saga.aop;

import java.net.InetAddress;
import java.util.Date;

import javax.annotation.Resource;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.github.tnessn.saga.dao.GlobalTxDao;
import com.github.tnessn.saga.dao.TxEventDao;
import com.github.tnessn.saga.enums.GlobalTxStatusEnum;
import com.github.tnessn.saga.model.GlobalTx;
import com.github.tnessn.saga.model.TxEvent;
import com.github.tnessn.saga.util.SagaUtils;
import com.github.tnessn.saga.util.UUIDUtils;

/**
 * @author huangjinfeng
 *
 */
@Service
@Aspect
public class SagaStartAspceJAdvice {
	
	private static final Logger logger = LoggerFactory.getLogger(SagaStartAspceJAdvice.class);
	
	@Resource
	private TxEventDao txEventDao;
	
	@Resource
	private GlobalTxDao globalTxDao;
	
	@Autowired
	private Environment env;
	
	
	@Pointcut("@annotation(com.github.tnessn.saga.aop.SagaStart)")
	public void pointcut() {};
	
	
	
	@Before(value = "pointcut()")
	public void beforeAdvice(JoinPoint joinPoint) throws Exception {
		logger.info("-------------" + joinPoint.getSignature().getName() + " start-------------");
		
		String globalTxId = UUIDUtils.get32UUID();
		
		Date now=new Date();
		GlobalTx globalTx=new GlobalTx();
		globalTx.setId(globalTxId);
		globalTx.setStatus(GlobalTxStatusEnum.PROCESSING.getCode());
		globalTx.setCreateTime(now);
		globalTxDao.save(globalTx);
		
		TxEvent txEvent=new TxEvent();
		txEvent.setGlobalTxId(globalTxId);
		txEvent.setCreateTime(now);
		txEvent.setServiceName(env.getProperty("spring.application.name", ""));
		txEvent.setIp(InetAddress.getLocalHost().getHostAddress());
		txEventDao.save(txEvent);
		
		SagaUtils.setGlobalTxId(globalTxId);
	}
	
	@AfterReturning(value = "pointcut()", returning = "retVal")
	public void afterReturningAdvice(JoinPoint joinPoint, Object retVal) {
		GlobalTx globalTx=new GlobalTx();
		globalTx.setId(SagaUtils.getGlobalTxId());
		globalTx.setUpdateTime(new Date());
		globalTx.setStatus(GlobalTxStatusEnum.FINISHED.getCode());
		globalTxDao.update(globalTx);
	}
	
	@AfterThrowing(value = "pointcut()", throwing = "ex")
	public void afterThrowingAdvice(JoinPoint joinPoint, Exception ex) {
		GlobalTx globalTx=new GlobalTx();
		globalTx.setId(SagaUtils.getGlobalTxId());
		globalTx.setUpdateTime(new Date());
		globalTx.setStatus(GlobalTxStatusEnum.ERROR.getCode());
		globalTxDao.update(globalTx);
	}
}