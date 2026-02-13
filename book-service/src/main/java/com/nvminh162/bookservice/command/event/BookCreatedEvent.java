package com.nvminh162.bookservice.command.event;

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
