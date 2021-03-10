package net.codejava;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@SpringBootApplication
public class HellospringApplication {

	public static void main(String[] args) {
		SpringApplication.run(HellospringApplication.class, args);
	}

}
