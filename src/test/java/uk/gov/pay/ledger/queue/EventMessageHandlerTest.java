package uk.gov.pay.ledger.queue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.ledger.event.services.EventService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EventMessageHandlerTest {

    @Mock
    private EventQueue eventQueue;

    @Mock
    private EventService eventService;

    @Mock
    private EventService.CreateEventResponse createEventResponse;

    private EventMessageHandler eventMessageHandler;

    @Before
    public void setUp() throws QueueException {
        var message = mock(EventMessage.class);

        when(eventQueue.retrieveEvents()).thenReturn(List.of(message));
        when(eventService.createIfDoesNotExist(any())).thenReturn(createEventResponse);

        eventMessageHandler = new EventMessageHandler(eventQueue, eventService);
    }

    @Test
    public void marksMessageAsProcessedWhenSuccessfulCreation() throws QueueException {
        when(createEventResponse.isSuccessful()).thenReturn(true);

        eventMessageHandler.handle();

        verify(eventQueue).markMessageAsProcessed(any());
    }

    @Test
    public void schedulesMessageForRetryWhenUnsuccessfulCreation() throws QueueException {
        when(createEventResponse.isSuccessful()).thenReturn(false);

        eventMessageHandler.handle();

        verify(eventQueue).scheduleMessageForRetry(any());
    }
}