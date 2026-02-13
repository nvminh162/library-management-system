package com.nvminh162.bookservice.command.event;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookDeletedEvent {
    String id;
}
