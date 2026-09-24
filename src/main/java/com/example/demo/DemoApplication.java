package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
        System.out.println("\n=======================================================");
        System.out.println("  ¡Aplicación Spring Boot iniciada correctamente!");
        System.out.println("  Conectado a MySQL (XAMPP): http://localhost:8080/api/conexion");
        System.out.println("=======================================================\n");
    }

}
