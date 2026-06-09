package com.nvminh162.book.command.event;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookCreatedEvent {
    String id;
    String name;
    String author;
    Boolean isReady;
}
