package com.telcox.billing.job;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;

/**
 * BILL-01 / FR-21: MonthlyBillRunJob'un Quartz JobDetail ve Trigger tanimlari.
 * Cron: her ayin 1'i, saat 02:00 (sunucu saat dilimi).
 * Job durable=true, yani hicbir trigger'a bagli olmasa bile scheduler'da kalir
 * (Quartz JDBC job store zaten kalicidir - bkz. V3__quartz_tables.sql).
 */
@Configuration
public class QuartzConfig {

    private static final String CRON_MONTHLY_AT_2AM_ON_1ST = "0 0 2 1 * ?";

    @Bean
    public JobDetailFactoryBean monthlyBillRunJobDetail() {
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(MonthlyBillRunJob.class);
        factoryBean.setName("monthlyBillRunJob");
        factoryBean.setGroup("billing");
        factoryBean.setDurability(true);
        return factoryBean;
    }

    @Bean
    public CronTriggerFactoryBean monthlyBillRunTrigger(JobDetailFactoryBean monthlyBillRunJobDetail) {
        CronTriggerFactoryBean trigger = new CronTriggerFactoryBean();
        trigger.setJobDetail(monthlyBillRunJobDetail.getObject());
        trigger.setName("monthlyBillRunTrigger");
        trigger.setGroup("billing");
        trigger.setCronExpression(CRON_MONTHLY_AT_2AM_ON_1ST);
        trigger.setMisfireInstructionName("MISFIRE_INSTRUCTION_FIRE_ONCE_NOW");
        return trigger;
    }
}
