package com.nvminh162.bookservice.query.model;


import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class _BookResponseModel {
    String id;
    String name;
    String author;
    Boolean isReady;
}
