package com.pivotal.task.gpdb.gpstager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = { "com.pivotal.task.gpdb"} )
public class GpstagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(GpstagerApplication.class, args);
	}
}
