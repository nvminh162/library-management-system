package com.nvminh162.discoverserver;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/discover-server")
public class DiscoverServerController {

    @GetMapping
    public String time(){
        return "(1) Current date time: " + java.time.LocalDateTime.now();
    }
}
