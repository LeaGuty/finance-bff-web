package cl.duoc.finance_bff_web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class FinanceBffWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinanceBffWebApplication.class, args);
    }

    // Configuración del Bean RestTemplate para inyección de dependencias
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}