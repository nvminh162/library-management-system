package com.nvminh162.bookservice.command.command;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateBookCommand {
    /*
    Là annotation để chỉ định trường này là định danh duy nhất cho aggregate
    Command này được chuyển đến Aggregate nào nó sẽ liên kết tới => muốn dispatch command tạo Book => dispatch tới Book Aggregate
    */
    @TargetAggregateIdentifier
    String id;
    String name;
    String author;
    Boolean isReady;
}
