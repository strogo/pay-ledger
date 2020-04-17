package uk.gov.pay.ledger.util.fixture;

import org.jdbi.v3.core.Jdbi;
import uk.gov.pay.ledger.payout.entity.PayoutEntity;

import java.time.ZonedDateTime;

import static uk.gov.pay.ledger.payout.entity.PayoutEntity.PayoutEntityBuilder.aPayoutEntity;

public class PayoutFixture implements DbFixture<PayoutFixture, PayoutEntity> {

    private Long id;
    private String gatewayPayoutId;
    private Long amount;
    private ZonedDateTime createdDate;
    private ZonedDateTime paidOutDate;
    private String statementDescriptor;
    private String status;
    private String type;
    private Long version;

    @Override
    public PayoutFixture insert(Jdbi jdbi) {
        jdbi.withHandle(handle ->
                handle.execute(
                        "INSERT INTO" +
                                " payouts(\n" +
                                "   id,\n" +
                                "   gateway_payout_id,\n" +
                                "   amount,\n" +
                                "   created_date,\n" +
                                "   paid_out_date,\n" +
                                "   statement_descriptor,\n" +
                                "   status,\n" +
                                "   type\n" +
                                " )\n" +
                                " VALUES (?,?,?,?,?,?,?,?)",
                        id, gatewayPayoutId, amount, createdDate, paidOutDate, statementDescriptor, status, type
                ));
        return this;
    }

    @Override
    public PayoutEntity toEntity() {
        return aPayoutEntity()
                .withAmount(amount)
                .withCreatedDate(createdDate)
                .withGatewayPayoutId(gatewayPayoutId)
                .withId(id)
                .withPaidOutDate(paidOutDate)
                .withStatementDescriptor(statementDescriptor)
                .withStatus(status)
                .withType(type)
                .build();
    }

    public static final class PayoutFixtureBuilder {
        private Long id;
        private String gatewayPayoutId;
        private Long amount;
        private ZonedDateTime createdDate;
        private ZonedDateTime paidOutDate;
        private String statementDescriptor;
        private String status;
        private String type;

        private PayoutFixtureBuilder() {
        }

        public static PayoutFixtureBuilder aPayoutFixture() {
            return new PayoutFixtureBuilder();
        }

        public PayoutFixtureBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public PayoutFixtureBuilder withGatewayPayoutId(String gatewayPayoutId) {
            this.gatewayPayoutId = gatewayPayoutId;
            return this;
        }

        public PayoutFixtureBuilder withAmount(Long amount) {
            this.amount = amount;
            return this;
        }

        public PayoutFixtureBuilder withCreatedDate(ZonedDateTime createdDate) {
            this.createdDate = createdDate;
            return this;
        }

        public PayoutFixtureBuilder withPaidOutDate(ZonedDateTime paidOutDate) {
            this.paidOutDate = paidOutDate;
            return this;
        }

        public PayoutFixtureBuilder withStatementDescriptor(String statementDescriptor) {
            this.statementDescriptor = statementDescriptor;
            return this;
        }

        public PayoutFixtureBuilder withStatus(String status) {
            this.status = status;
            return this;
        }

        public PayoutFixtureBuilder withType(String type) {
            this.type = type;
            return this;
        }

        public PayoutFixture build() {
            PayoutFixture payoutFixture = new PayoutFixture();
            payoutFixture.amount = this.amount;
            payoutFixture.gatewayPayoutId = this.gatewayPayoutId;
            payoutFixture.id = this.id;
            payoutFixture.status = this.status;
            payoutFixture.statementDescriptor = this.statementDescriptor;
            payoutFixture.createdDate = this.createdDate;
            payoutFixture.paidOutDate = this.paidOutDate;
            payoutFixture.type = this.type;
            return payoutFixture;
        }
    }
}