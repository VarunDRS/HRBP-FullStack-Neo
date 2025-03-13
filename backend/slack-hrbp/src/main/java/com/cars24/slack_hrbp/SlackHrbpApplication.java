package com.cars24.slack_hrbp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class SlackHrbpApplication {

//	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application){
		return application.sources(SlackHrbpApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(SlackHrbpApplication.class, args);
	}

	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder(){
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SpringApplicationContext springApplicationContext(){
		return new SpringApplicationContext();
	}

}
