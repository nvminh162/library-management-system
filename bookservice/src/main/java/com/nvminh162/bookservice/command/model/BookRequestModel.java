package com.nvminh162.bookservice.command.model;


import lombok.*;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookRequestModel {
    String id;
    String name;
    String author;
    Boolean isReady;
}
