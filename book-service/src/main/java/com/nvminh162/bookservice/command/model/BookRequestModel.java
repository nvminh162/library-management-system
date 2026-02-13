package com.nvminh162.bookservice.command.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookRequestModel {
    String id;
    String name;
    String author;
    Boolean isReady;
}
