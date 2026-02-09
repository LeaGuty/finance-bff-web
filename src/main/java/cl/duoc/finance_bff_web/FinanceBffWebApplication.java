package cl.duoc.finance_bff_web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * Clase principal de la aplicacion Finance BFF Web.
 *
 * Este microservicio actua como Backend-For-Frontend (BFF) para clientes web,
 * proporcionando una capa intermedia entre el frontend y los microservicios
 * backend (finance-batch). Combina y adapta las respuestas de multiples
 * endpoints del backend en un unico formato optimizado para el consumo web.
 *
 * Funcionalidades principales:
 * - Autenticacion JWT para clientes web con rol CLIENTE_WEB
 * - Agregacion de datos de cuentas y transacciones desde finance-batch
 * - Comunicacion HTTPS segura (puerto 8081)
 *
 * @author Desarrollo DUOC
 * @version 0.0.1-SNAPSHOT
 */
@SpringBootApplication
public class FinanceBffWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinanceBffWebApplication.class, args);
    }

    /**
     * Bean de RestTemplate utilizado para realizar llamadas HTTP
     * hacia los microservicios backend (finance-batch en puerto 8080).
     *
     * Se inyecta en FinanceWebServiceImpl para las consultas a la API REST.
     *
     * @return instancia de RestTemplate configurada por defecto
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
