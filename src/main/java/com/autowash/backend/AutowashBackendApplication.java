package com.autowash.backend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import java.util.Arrays;
import org.springframework.core.env.Environment;
@SpringBootApplication
public class AutowashBackendApplication {

	public static void main(String[] args) {
		
		SpringApplication.run(AutowashBackendApplication.class, args);
	}
}

