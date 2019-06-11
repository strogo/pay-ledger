package uk.gov.pay.ledger.transaction.search.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class TransactionSearchParams {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionSearchParams.class);

    private static final String GATEWAY_ACCOUNT_EXTERNAL_FIELD = "gatewayAccountExternalId";
    private static final String OFFSET_FIELD = "offset";
    private static final String PAGE_SIZE_FIELD = "limit";
    private static final String CARDHOLDER_NAME_FIELD = "cardholder_name";
    private static final String FROM_DATE_FIELD = "from_date";
    private static final String TO_DATE_FIELD = "to_date";
    private static final String EMAIL_FIELD = "email";
    private static final String REFERENCE_FIELD = "reference";
    private static final String LAST_DIGITS_CARD_NUMBER_KEY = "last_digits_card_number";
    private static final String FIRST_DIGITS_CARD_NUMBER_KEY = "first_digits_card_number";
    private static final String PAYMENT_STATES_KEY = "payment_states";
    private static final String REFUND_STATES_KEY = "refund_states";
    private static final String CARD_BRAND_KEY = "card_brand";
    private static final long MAX_DISPLAY_SIZE = 500;

    private String accountId;
    private String email;
    private String reference;
    private String cardHolderName;
    private String lastDigitsCardNumber;
    private String firstDigitsCardNumber;
    private CommaDelimitedSetParameter paymentStates;
    private CommaDelimitedSetParameter refundStates;
    private List<String> cardBrands;
    private ZonedDateTime fromDate;
    private ZonedDateTime toDate;
    private Long pageNumber = 1L;
    private Long displaySize = MAX_DISPLAY_SIZE;

    private Map<String, Object> queryMap;

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    public void setLastDigitsCardNumber(String lastDigitsCardNumber) {
        this.lastDigitsCardNumber = lastDigitsCardNumber;
    }

    public void setFirstDigitsCardNumber(String firstDigitsCardNumber) {
        this.firstDigitsCardNumber = firstDigitsCardNumber;
    }

    public void setPaymentStates(CommaDelimitedSetParameter paymentStates) {
        this.paymentStates = paymentStates;
    }

    public void setRefundStates(CommaDelimitedSetParameter refundStates) {
        this.refundStates = refundStates;
    }

    public void setCardBrands(List<String> cardBrands) {
        this.cardBrands = cardBrands;
    }

    public void setFromDate(ZonedDateTime fromDate) {
        this.fromDate = fromDate;
    }

    public void setToDate(ZonedDateTime toDate) {
        this.toDate = toDate;
    }

    public void setPageNumber(long pageNumber) {
        this.pageNumber = pageNumber;
    }

    public void setDisplaySize(long displaySize) {
        if (displaySize > MAX_DISPLAY_SIZE) {
            LOGGER.info("Invalid display_size [{}] for transaction search params. Setting display_size to default [{}]",
                    displaySize, MAX_DISPLAY_SIZE);
            this.displaySize = MAX_DISPLAY_SIZE;
        } else {
            this.displaySize = displaySize;
        }
    }

    public String generateQuery() {
        StringBuilder sb = new StringBuilder();

        if (isNotBlank(email)) {
            sb.append(" AND t.email ILIKE :" + EMAIL_FIELD);
        }
        if (isNotBlank(reference)) {
            sb.append(" AND t.reference ILIKE :" + REFERENCE_FIELD);
        }
        if (isNotBlank(cardHolderName)) {
            sb.append(" AND t.cardholder_name ILIKE :" + CARDHOLDER_NAME_FIELD);
        }

        if (fromDate != null) {
            sb.append(" AND t.created_date > :" + FROM_DATE_FIELD);
        }
        if (toDate != null) {
            sb.append(" AND t.created_date < :" + TO_DATE_FIELD);
        }
        if (paymentStates != null && !paymentStates.isEmpty()) {

            if (paymentStates.has("created")) {
                sb.append(" AND t.status in ('created')");
            } else {
                sb.append(" AND false");
            }
        }

        if (refundStates != null) {
            sb.append(" AND false");
        }
        if (cardBrands != null) {
            sb.append(" AND false");
        }
        if (isNotBlank(lastDigitsCardNumber)) {
            sb.append(" AND false");
        }
        if (isNotBlank(firstDigitsCardNumber)) {
            sb.append(" AND false");
        }

        return sb.toString();
    }

    public Map<String, Object> getQueryMap() {
        if (queryMap == null) {
            queryMap = new HashMap<>();
            queryMap.put(GATEWAY_ACCOUNT_EXTERNAL_FIELD, accountId);
            queryMap.put(OFFSET_FIELD, getOffset());
            queryMap.put(PAGE_SIZE_FIELD, displaySize);

            if (isNotBlank(email)) {
                queryMap.put(EMAIL_FIELD, likeClause(email));
            }
            if (isNotBlank(reference)) {
                queryMap.put(REFERENCE_FIELD, likeClause(reference));
            }
            if (cardHolderName != null) {
                queryMap.put(CARDHOLDER_NAME_FIELD, cardHolderName);
            }
            if (fromDate != null) {
                queryMap.put(FROM_DATE_FIELD, fromDate);
            }
            if (toDate != null) {
                queryMap.put(TO_DATE_FIELD, toDate);
            }
        }
        return queryMap;
    }

    public String getAccountId() {
        return accountId;
    }

    public Long getPageNumber() {
        return pageNumber;
    }

    public Long getDisplaySize() {
        return displaySize;
    }

    private Long getOffset() {
        Long offset = 0l;

        if (pageNumber != null) {
            offset = (pageNumber - 1) * getDisplaySize();
        }

        return offset;
    }


    private String likeClause(String rawUserInputText) {
        return "%" + rawUserInputText + "%";
    }

    public String buildQueryParamString(Long forPage) {
        String query = "";

        if (fromDate != null) {
            query += "&" + FROM_DATE_FIELD + "=" + fromDate;
        }
        if (toDate != null) {
            query += "&" + TO_DATE_FIELD + "=" + toDate;
        }

        if (isNotBlank(email)) {
            query += "&" + EMAIL_FIELD + "=" + email;
        }
        if (isNotBlank(reference)) {
            query += "&" + REFERENCE_FIELD + "=" + reference;
        }
        if (isNotBlank(cardHolderName)) {
            query += "&" + CARDHOLDER_NAME_FIELD + "=" + cardHolderName;
        }
        if (isNotBlank(firstDigitsCardNumber)) {
            query += "&" + FIRST_DIGITS_CARD_NUMBER_KEY + "=" + firstDigitsCardNumber;
        }
        if (isNotBlank(lastDigitsCardNumber)) {
            query += "&" + LAST_DIGITS_CARD_NUMBER_KEY + "=" + lastDigitsCardNumber;
        }

        if (paymentStates != null && !paymentStates.isEmpty()) {
            query += "&" + PAYMENT_STATES_KEY + "=" + String.join(",", this.paymentStates.getRawString());
        }
        if (refundStates != null && !refundStates.isEmpty()) {
            query += "&" + REFUND_STATES_KEY + "=" + String.join(",", this.refundStates.getRawString());
        }
        if (paymentStates != null && !paymentStates.isEmpty()) {
            query += "&" + CARD_BRAND_KEY + "=" + String.join(",", this.cardBrands);
        }

        query += addPaginationParams(forPage);
        return query.substring(1);
    }

    private String addPaginationParams(Long forPage) {
        String queryParams = format("&page=%s", forPage);
        queryParams += format("&display_size=%s", displaySize);
        return queryParams;
    }
}