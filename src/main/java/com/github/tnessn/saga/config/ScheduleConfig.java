package com.github.tnessn.saga.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration("sagaScheduleConfig")
public class ScheduleConfig{
	
	private final Logger LOG = LoggerFactory.getLogger(this.getClass());
	
	@Bean("sagaScheduler")
	public ThreadPoolTaskScheduler scheduler() {
		ThreadPoolTaskScheduler scheduler=new ThreadPoolTaskScheduler();
		scheduler.setPoolSize(5);
		scheduler.setThreadNamePrefix("saga-scheduler-");
		scheduler.setAwaitTerminationSeconds(60);
		scheduler.setWaitForTasksToCompleteOnShutdown(true);
		scheduler.setRemoveOnCancelPolicy(true);
		return scheduler;
	}
}