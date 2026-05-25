package com.example.xinlingqian;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.xinlingqian.mapper")
public class XinlingqianApplication {

	public static void main(String[] args) {
		SpringApplication.run(XinlingqianApplication.class, args);
	}

}
