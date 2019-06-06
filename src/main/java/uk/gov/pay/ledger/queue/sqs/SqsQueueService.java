package uk.gov.pay.ledger.queue.sqs;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.AmazonSQSException;
import com.amazonaws.services.sqs.model.ChangeMessageVisibilityRequest;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.DeleteMessageResult;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.ledger.app.LedgerConfig;
import uk.gov.pay.ledger.queue.QueueException;
import uk.gov.pay.ledger.queue.QueueMessage;

import javax.inject.Inject;
import java.util.List;

public class SqsQueueService {
    private final Logger logger = LoggerFactory.getLogger(SqsQueueService.class);

    private AmazonSQS sqsClient;

    private final int messageMaximumWaitTimeInSeconds;
    private final int messageMaximumBatchSize;

    @Inject
    public SqsQueueService(AmazonSQS sqsClient, LedgerConfig ledgerConfig) {
        this.sqsClient = sqsClient;
        this.messageMaximumBatchSize = ledgerConfig.getSqsConfig().getMessageMaximumBatchSize();
        this.messageMaximumWaitTimeInSeconds = ledgerConfig.getSqsConfig().getMessageMaximumWaitTimeInSeconds();
    }

    public List<QueueMessage> receiveMessages(String queueUrl, String messageAttributeName) throws QueueException {
        try {
            ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl);
            receiveMessageRequest
                    .withMessageAttributeNames(messageAttributeName)
                    .withWaitTimeSeconds(messageMaximumWaitTimeInSeconds)
                    .withMaxNumberOfMessages(messageMaximumBatchSize);

            ReceiveMessageResult receiveMessageResult = sqsClient.receiveMessage(receiveMessageRequest);

            return QueueMessage.of(receiveMessageResult);
        } catch (AmazonSQSException | UnsupportedOperationException e) {
            logger.error("Failed to receive messages from SQS queue - [{}] {}", e.getClass().getCanonicalName(), e.getMessage());
            throw new QueueException(e.getMessage());
        }
    }

    public void deleteMessage(String queueUrl, String messageReceiptHandle) throws QueueException {
        try {
            sqsClient.deleteMessage(new DeleteMessageRequest(queueUrl, messageReceiptHandle));
        } catch (AmazonSQSException | UnsupportedOperationException e) {
            logger.error("Failed to delete message from SQS queue - {}", e.getMessage());
            throw new QueueException(e.getMessage());
        } catch (AmazonServiceException e) {
            logger.error("Failed to delete message from SQS queue - [errorMessage={}] [awsErrorCode={}]", e.getMessage(), e.getErrorCode());
            String errorMessage = String.format("%s [%s]", e.getMessage(), e.getErrorCode());
            throw new QueueException(errorMessage);
        }
    }

    public void deferMessage(String queueUrl, String messageReceiptHandle, int retryDelayInSeconds) throws QueueException {
        try {
            ChangeMessageVisibilityRequest changeMessageVisibilityRequest = new ChangeMessageVisibilityRequest(
                    queueUrl,
                    messageReceiptHandle,
                    retryDelayInSeconds
            );

            sqsClient.changeMessageVisibility(changeMessageVisibilityRequest);
        } catch (AmazonSQSException | UnsupportedOperationException e) {
            logger.error("Failed to defer message from SQS queue - {}", e.getMessage());
            throw new QueueException(e.getMessage());
        }
    }
}
