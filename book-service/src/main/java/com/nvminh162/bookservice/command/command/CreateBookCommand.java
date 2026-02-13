package com.nvminh162.bookservice.command.command;


import lombok.*;
import lombok.experimental.FieldDefaults;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateBookCommand {
    @TargetAggregateIdentifier
    String id;

    String name;

    String author;

    Boolean isReady;
}
