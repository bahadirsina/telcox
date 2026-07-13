package com.telcox.billing.job;

import com.telcox.billing.service.BillRunService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * BILL-01 / FR-21: Her ayin 1'i saat 02:00'de calisan bill-run job'u.
 * Quartz DB job store (V3__quartz_tables.sql) kullanildigi icin instance
 * yeniden baslasa bile job kaybolmaz/tekrarlanmaz (misfire policy Quartz'in
 * kendi mekanizmasina birakilmistir).
 *
 * NOT: Quartz job instance'lari Quartz tarafindan olusturuldugu icin
 * constructor injection yerine @Autowired field/setter injection kullanilir
 * (bkz. QuartzConfig - SpringBeanJobFactory).
 */
public class MonthlyBillRunJob implements Job {

    private static final Logger log = LoggerFactory.getLogger(MonthlyBillRunJob.class);

    @Autowired
    private BillRunService billRunService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        YearMonth period = BillRunService.previousMonth(LocalDate.now());
        log.info("MonthlyBillRunJob triggered for period {}", period);
        try {
            BillRunService.BillRunSummary summary = billRunService.runForPeriod(period);
            log.info("MonthlyBillRunJob finished: {}", summary);
        } catch (Exception e) {
            log.error("MonthlyBillRunJob failed for period {}", period, e);
            throw new JobExecutionException(e);
        }
    }
}
