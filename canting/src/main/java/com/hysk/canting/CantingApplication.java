package com.hysk.canting;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.hysk.canting.mapper")
public class CantingApplication {

	public static void main(String[] args) {
		SpringApplication.run(CantingApplication.class, args);

	}

}
