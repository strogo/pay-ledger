package uk.gov.pay.ledger.report.dao;

import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.pay.ledger.rule.AppWithPostgresAndSqsRule;
import uk.gov.pay.ledger.transaction.state.TransactionState;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Stream;

import static java.math.BigDecimal.ZERO;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.BigDecimalCloseTo.closeTo;
import static uk.gov.pay.ledger.util.fixture.TransactionFixture.aTransactionFixture;

public class PerformanceReportDaoIT {

    @ClassRule
    public static AppWithPostgresAndSqsRule rule = new AppWithPostgresAndSqsRule();

    private PerformanceReportDao transactionDao = new PerformanceReportDao(rule.getJdbi());

    @Test
    public void report_volume_total_amount_and_average_amount_for_date_range() {
        Stream.of("2019-12-31T10:00:00Z", "2019-12-30T10:00:00Z", "2017-11-29T10:00:00Z").forEach(time -> aTransactionFixture()
                .withCreatedDate(ZonedDateTime.parse(time))
                .withAmount(100L)
                .withState(TransactionState.SUCCESS)
                .withTransactionType("PAYMENT")
                .withLive(true)
                .insert(rule.getJdbi()));

        Stream.of("2019-12-12T10:00:00Z", "2019-12-11T10:00:00Z", "2017-11-30T10:00:00Z").forEach(time -> aTransactionFixture()
                .withCreatedDate(ZonedDateTime.parse(time))
                .withAmount(1000L)
                .withState(TransactionState.SUCCESS)
                .withTransactionType("PAYMENT")
                .withLive(true)
                .insert(rule.getJdbi()));

        var performanceReportEntity = transactionDao.performanceReportForPaymentTransactions(
                ZonedDateTime.parse("2017-11-30T10:00:00Z"), ZonedDateTime.parse("2019-12-12T10:00:00Z"));
        assertThat(performanceReportEntity.getTotalVolume(), is(3L));
        assertThat(performanceReportEntity.getTotalAmount(), is(closeTo(new BigDecimal(3000L), ZERO)));
        assertThat(performanceReportEntity.getAverageAmount(), is(closeTo(new BigDecimal(1000L), ZERO)));
    }

    @Test
    public void report_volume_total_amount_and_average_amount() {
        aTransactionFixture().withAmount(1000L).withState(TransactionState.STARTED).withTransactionType("PAYMENT")
                .withLive(true).insert(rule.getJdbi());

        aTransactionFixture().withAmount(1000L).withState(TransactionState.SUCCESS).withTransactionType("REFUND")
                .withLive(true).insert(rule.getJdbi());

        aTransactionFixture().withAmount(1000L).withState(TransactionState.SUCCESS).withTransactionType("PAYMENT")
                .withLive(false).insert(rule.getJdbi());

        List<Long> relevantAmounts = List.of(1200L, 1020L, 750L);
        relevantAmounts.stream().forEach(amount -> aTransactionFixture()
                .withAmount(amount)
                .withState(TransactionState.SUCCESS)
                .withTransactionType("PAYMENT")
                .withLive(true)
                .insert(rule.getJdbi()));
        BigDecimal expectedTotalAmount = new BigDecimal(relevantAmounts.stream().mapToLong(amount -> amount).sum());
        BigDecimal expectedAverageAmount = new BigDecimal(relevantAmounts.stream().mapToLong(amount -> amount).average().getAsDouble());

        var performanceReportEntity = transactionDao.performanceReportForPaymentTransactions();
        assertThat(performanceReportEntity.getTotalVolume(), is(3L));
        assertThat(performanceReportEntity.getTotalAmount(), is(closeTo(expectedTotalAmount, ZERO)));
        assertThat(performanceReportEntity.getAverageAmount(), is(closeTo(expectedAverageAmount, ZERO)));
    }
}