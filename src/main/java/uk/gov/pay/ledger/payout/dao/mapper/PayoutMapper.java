package uk.gov.pay.ledger.payout.dao.mapper;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import uk.gov.pay.ledger.payout.entity.PayoutEntity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

public class PayoutMapper implements RowMapper<PayoutEntity> {

    @Override
    public PayoutEntity map(ResultSet rs, StatementContext ctx) throws SQLException {
       var builder = PayoutEntity.PayoutEntityBuilder.aPayoutEntity()
               .withId(rs.getLong("id"))
               .withGatewayPayoutId(rs.getString("gateway_payout_id"))
               .withAmount(rs.getLong("amount"))
               .withStatementDescriptor(rs.getString("statement_descriptor"))
               .withType(rs.getString("type"))
               .withStatus(rs.getString("status"))
               .withCreatedDate(getZonedDateTime(rs, "created_date").orElse(null))
               .withPaidOutDate(getZonedDateTime(rs, "paid_out_date").orElse(null));
        return builder.build();
    }

    private Optional<ZonedDateTime> getZonedDateTime(ResultSet rs, String columnLabel) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(columnLabel);

        return Optional.ofNullable(timestamp)
                .map(t -> ZonedDateTime.ofInstant(t.toInstant(), ZoneOffset.UTC));
    }
}