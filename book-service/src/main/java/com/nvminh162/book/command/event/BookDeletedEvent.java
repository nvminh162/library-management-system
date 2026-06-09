package com.nvminh162.book.command.event;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookDeletedEvent {
    String id;
}
