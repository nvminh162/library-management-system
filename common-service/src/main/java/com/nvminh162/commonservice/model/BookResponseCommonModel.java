package com.nvminh162.commonservice.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookResponseCommonModel {
    String id;
    String name;
    String author;
    Boolean isReady;
}
