package com.nvminh162.borrow.command.saga;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.spring.stereotype.Saga;

import com.nvminh162.borrow.command.command.DeleteBorrowingCommand;
import com.nvminh162.borrow.command.event.BorrowingCreatedEvent;
import com.nvminh162.borrow.command.event.BorrowingDeletedEvent;
import com.nvminh162.contract.command.UpdateStatusBookCommand;
import com.nvminh162.contract.command.RollBackStatusBookCommand;
import com.nvminh162.contract.event.BookUpdatedStatusEvent;
import com.nvminh162.contract.event.BookRollBackStatusEvent;
import com.nvminh162.contract.model.BookResponseCommonModel;
import com.nvminh162.contract.model.EmployeeResponseCommonModel;
import com.nvminh162.contract.queries.GetBookDetailQuery;
import com.nvminh162.contract.queries.GetDetailEmployeeQuery;

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

    // (a.1) or (b.1)
    @StartSaga
    @SagaEventHandler(associationProperty = "id")
    private void handle(BorrowingCreatedEvent event) {
        log.info("(i) >>>>>>>>> Borrowing created event in saga for BookId: {} & EmployeeId: {}", event.getBookId(), event.getEmployeeId());
        try {
            GetBookDetailQuery query = new GetBookDetailQuery(event.getBookId());
            BookResponseCommonModel model = queryGateway.query(
                query,
                ResponseTypes.instanceOf(BookResponseCommonModel.class)
            ).join();

            if(!model.getIsReady()) {
                // (a.2)
                throw new Exception("(x) >>>>>>>>>> This book has already been borrowed!");
            } else {
                // (b.2)
                SagaLifecycle.associateWith("bookId", event.getBookId());
                UpdateStatusBookCommand updateStatusBookCommand = new UpdateStatusBookCommand(event.getBookId(), false, event.getEmployeeId(), event.getId());
                commandGateway.sendAndWait(updateStatusBookCommand);
            }

        } catch (Exception e) {
            // (a.3)
            log.error(e.getMessage());
            rollbackBorrowingRecord(event.getId());
            // TODO: handle exception
        }
    }

    // (b.3)
    @SagaEventHandler(associationProperty = "bookId")
    private void handle(BookUpdatedStatusEvent event) {
        log.info("(i) >>>>>>>>> Book updated status event in saga for BookId: {}", event.getBookId());
        
        try {
            GetDetailEmployeeQuery query = new GetDetailEmployeeQuery(event.getEmployeeId());
            EmployeeResponseCommonModel model = queryGateway.query(
                query,
                ResponseTypes.instanceOf(EmployeeResponseCommonModel.class)
            ).join();
            if (model.getIsDisciplined()) {
                // (b.4.1)
                throw new Exception("(x) >>>>>>>>>> Employee has been disciplined!");
            } else {
                // (b.4.2)
                log.info("(i) >>>>>>>>> You have borrow book successfully: {}", event.getBookId());
                SagaLifecycle.end();
            }
        } catch (Exception e) {
            // (b.4.1.2)
            rollbackBookStatus(event.getBookId(), event.getEmployeeId(), event.getBorrowingId());
            log.error(e.getMessage());
        }
    }

    // (b.4.1.4)
    @SagaEventHandler(associationProperty = "bookId")
    private void handle(BookRollBackStatusEvent event) {
        log.info("(i) >>>>>>>>> Book roll back status event in saga for book ID: {}", event.getBookId());
        rollbackBorrowingRecord(event.getBorrowingId());
    }

    // (a.5) or (b.4.1.6)
    @SagaEventHandler(associationProperty = "id")
    @EndSaga
    private void handle(BorrowingDeletedEvent event) {
        log.info("(i) >>>>>>>>> Borrowing deleted event in Saga for Borrowing ID: {}", event.getId());
        SagaLifecycle.end();
    }

    // (a.4) or (b.4.1.5)
    private void rollbackBorrowingRecord(String id) {
        DeleteBorrowingCommand command = new DeleteBorrowingCommand(id);
        commandGateway.sendAndWait(command);
    }

    // (b.4.1.3)
    private void rollbackBookStatus(String bookId, String employeeId, String borrowingId) {
        SagaLifecycle.associateWith("bookId", bookId);
        RollBackStatusBookCommand command = new RollBackStatusBookCommand(
            bookId,
            true,
            employeeId,
            borrowingId
        );
        commandGateway.sendAndWait(command);
    }
}
