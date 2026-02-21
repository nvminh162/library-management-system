package com.nvminh162.borrowingservice.command.saga;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.spring.stereotype.Saga;

import com.nvminh162.borrowingservice.command.command.DeleteBorrowingCommand;
import com.nvminh162.borrowingservice.command.event.BorrowingCreatedEvent;
import com.nvminh162.commonservice.command.UpdateStatusBookCommand;
import com.nvminh162.commonservice.event.BookUpdatedStatusEvent;
import com.nvminh162.commonservice.model.BookResponseCommonModel;
import com.nvminh162.commonservice.queries.GetBookDetailQuery;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Saga
public class BorrowingSaga {

    @Autowired
    transient CommandGateway commandGateway;
    @Autowired
    transient QueryGateway queryGateway;

    @StartSaga
    @SagaEventHandler(associationProperty = "id")
    private void handle(BorrowingCreatedEvent event) {
        log.info("(i) >>>>>>>>> Borrowing created event in saga for BookId: {} & EmployeeId: {}", event.getBookId(), event.getEmployeeId());
        try {
            GetBookDetailQuery getBookDetailQuery = new GetBookDetailQuery(event.getBookId());
            BookResponseCommonModel bookResponseCommonModel = queryGateway.query(
                getBookDetailQuery,
                ResponseTypes.instanceOf(BookResponseCommonModel.class)
            ).join();

            if(!bookResponseCommonModel.getIsReady()) {
                throw new Exception("(x) >>>>>>>>>> This book has already been borrowed");
            } else {
                SagaLifecycle.associateWith("bookId", event.getBookId());
                UpdateStatusBookCommand updateStatusBookCommand = new UpdateStatusBookCommand(event.getBookId(), false, event.getEmployeeId(), event.getId());
                commandGateway.sendAndWait(updateStatusBookCommand);
            }

        } catch (Exception e) {
            log.error(e.getMessage());
            rollbackBorrowingRecord(event.getId());
            // TODO: handle exception
        }
    }

    @SagaEventHandler(associationProperty = "bookId")
    private void handle(BookUpdatedStatusEvent event) {
        log.info("(i) >>>>>>>>> Book updated status event in saga for BookId: {}", event.getBookId());
        SagaLifecycle.end();
    }

    public void rollbackBorrowingRecord(String id) {
        DeleteBorrowingCommand command = new DeleteBorrowingCommand(id);
        commandGateway.sendAndWait(command);
        SagaLifecycle.end();
    }
}
