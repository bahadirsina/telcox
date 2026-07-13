package com.telcox.billing.api;

import com.telcox.billing.service.BillRunService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * BILL-01: Bill-run'i Quartz'i beklemeden manuel tetiklemek icin (test/operasyon amacli).
 * Otomatik calisma zaten QuartzConfig + MonthlyBillRunJob uzerinden her ayin 1'i saat 02:00'de olur.
 */
@RestController
@RequestMapping("/api/v1/bill-runs")
public class BillRunController {

    private final BillRunService billRunService;

    public BillRunController(BillRunService billRunService) {
        this.billRunService = billRunService;
    }

    @PostMapping
    public BillRunService.BillRunSummary trigger(@RequestBody(required = false) BillRunRequest request) {
        YearMonth period = (request == null || request.period() == null)
                ? BillRunService.previousMonth(LocalDate.now())
                : request.period();
        return billRunService.runForPeriod(period);
    }
}
