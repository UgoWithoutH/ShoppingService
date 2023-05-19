package com.example.ShoppingService;

import com.example.ShoppingService.utils.HttpRequestHelper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class ShoppingServiceApplication {

	@Bean
	public WebClient webClient() {
		return WebClient.create();
	}

	@Bean
	public HttpRequestHelper httpRequestHelper(){
		return new HttpRequestHelper();
	}

	public static void main(String[] args) {
		SpringApplication.run(ShoppingServiceApplication.class, args);
	}

}
