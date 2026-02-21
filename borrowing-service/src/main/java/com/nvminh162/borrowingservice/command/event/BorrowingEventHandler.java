package com.nvminh162.borrowingservice.command.event;

import java.util.Optional;

import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

import com.nvminh162.borrowingservice.command.data.Borrowing;
import com.nvminh162.borrowingservice.command.data.BorrowingRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class BorrowingEventHandler {
    
    BorrowingRepository borrowingRepository;

    @EventHandler
    public void on(BorrowingCreatedEvent event) {
        Borrowing borrowing = new Borrowing();
        borrowing.setId(event.getId());
        borrowing.setBookId(event.getBookId());
        borrowing.setEmployeeId(event.getEmployeeId());
        borrowing.setBorrowingDate(event.getBorrowingDate());
        borrowingRepository.save(borrowing);
    }

    @EventHandler
    public void on(BorrowingDeletedEvent event) {
        Optional<Borrowing> optionalBorrowing = borrowingRepository.findById(event.getId());
        optionalBorrowing.ifPresent(borrowing -> borrowingRepository.delete(borrowing));
    }
}
