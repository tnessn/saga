package com.github.tnessn.saga.config;

import java.lang.invoke.MethodHandles;
import java.time.Duration;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.CollectionUtils;

import com.github.tnessn.saga.dao.GlobalTxDao;
import com.github.tnessn.saga.dao.TxEventDao;
import com.github.tnessn.saga.enums.GlobalTxStatusEnum;
import com.github.tnessn.saga.model.GlobalTx;
import com.github.tnessn.saga.model.TxEvent;
import com.github.tnessn.saga.util.MongoDistributedLock;
import com.github.tnessn.saga.util.SerializeUtils;

/**
 * 补偿定时任务
 * 
 * @author huangjinfeng
 */
@Configuration
@EnableScheduling
@DependsOn("sagaBeanLocator")
public class ScheduleJob {

	@Resource
	private GlobalTxDao globalTxDao;
	@Resource
	private TxEventDao txEventDao;
	@Resource
	private CallbackContext callbackContext;
	@Autowired
	private Environment environment;
	@Resource(name = "sagaScheduler")
	private ThreadPoolTaskScheduler scheduler;

	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final long ERROR_WAIT_TIME = 30 * 1000; // 异常事务等待30s
	private static final long PROCESSING_WAIT_TIME = 2 * 60 * 1000; // 超时事务等待2min
	private static final long LOCK_EXPIRE_TIME = 2 * 60 * 1000; // 默认2min
	private static final int FINISH_2_CLEAN_TIME = 5 * 60000; // 事务完成5分钟后清理
	private static final int DEFAULT_LIMIT = 1000; // 默认查询1000条

	@PostConstruct
	public void run() {
		scheduler.scheduleAtFixedRate(() -> errorCompensation(), Duration.ofSeconds(1));
		scheduler.scheduleAtFixedRate(() -> processingCompensation(), Duration.ofSeconds(1));
		scheduler.scheduleAtFixedRate(() -> cleanMongo(), Duration.ofSeconds(60));
	}

	@PreDestroy
	public void preDestroy() {
		LOG.error("sagaScheduler优雅关闭");
		scheduler.shutdown();
	}

	public void errorCompensation() {
		String serviceName = environment.getProperty("spring.application.name");
		MongoDistributedLock.tryLock(String.format("%s_%s", "errorCompensation", serviceName), LOCK_EXPIRE_TIME, () -> {
			LOG.error("异常事务补偿start.....................................");
			List<GlobalTx> errorList = globalTxDao.find(GlobalTxStatusEnum.ERROR.getCode(), 0, DEFAULT_LIMIT, Direction.ASC);
			errorList.parallelStream().forEach(globalTx -> {
				startCompensate(serviceName, globalTx, ERROR_WAIT_TIME);
			});
		});
	}

	public void processingCompensation() {
		String serviceName = environment.getProperty("spring.application.name");
		MongoDistributedLock.tryLock(String.format("%s_%s", "processingCompensation", serviceName), LOCK_EXPIRE_TIME, () -> {
			LOG.error("超时事务补偿start.....................................");
			List<GlobalTx> processingList = globalTxDao.find(GlobalTxStatusEnum.PROCESSING.getCode(), PROCESSING_WAIT_TIME, DEFAULT_LIMIT, Direction.ASC);
			processingList.parallelStream().forEach(globalTx -> {
				startCompensate(serviceName, globalTx, 0);
			});
		});
	}

	public void cleanMongo() {
		MongoDistributedLock.tryLock("CleanMongo", LOCK_EXPIRE_TIME, () -> {
			LOG.error("清理Mongo数据start.....................................");
			for (int i = 0; i < 10; i++) {
				List<GlobalTx> list = globalTxDao.findAndRemoveCompensatedAndFinish(DEFAULT_LIMIT, FINISH_2_CLEAN_TIME);
				if (CollectionUtils.isEmpty(list)) {
					break;
				}
				txEventDao.deleteByGlobalTxId(list.stream().map(e -> e.getId()).toArray(String[]::new));
			}
		});
	}

	private void startCompensate(String serviceName, GlobalTx globalTx, long waitTime) {
		boolean isCompensated = true;
		List<TxEvent> txEventList = txEventDao.findUncompensatedByGlobalTxId(globalTx.getId());
		for (TxEvent txEvent : txEventList) {
			// 没有补偿方法，不需要补偿
			if (StringUtils.isBlank(txEvent.getCompensationMethod())) {
				continue;
			}
			if (!txEvent.getServiceName().equals(serviceName)) {
				isCompensated = false;
				continue;
			}
			callbackContext.apply(txEvent.getCompensationMethod(), (Object[]) SerializeUtils.deserialize(txEvent.getPayload()));
			txEvent.setCompensated(true);
			txEvent.setUpdateTime(new Date());
			txEventDao.update(txEvent);
		}
		if (isCompensated && globalTx.getCreateTime().getTime() + waitTime <= System.currentTimeMillis()) {
			globalTx.setStatus(GlobalTxStatusEnum.COMPENSATED.getCode());
			globalTx.setUpdateTime(new Date());
			globalTxDao.update(globalTx);
		}
	}
}