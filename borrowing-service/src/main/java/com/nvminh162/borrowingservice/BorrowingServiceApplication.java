package com.nvminh162.borrowingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;	

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan({"com.nvminh162.borrowingservice", "com.nvminh162.commonservice"})
public class BorrowingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BorrowingServiceApplication.class, args);
	}

}
