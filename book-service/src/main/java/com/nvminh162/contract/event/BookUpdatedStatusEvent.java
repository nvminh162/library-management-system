package com.nvminh162.contract.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookUpdatedStatusEvent {

    private String bookId;
    private Boolean isReady;
    private String employeeId;
    private String borrowingId;
}
