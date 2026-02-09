package cl.duoc.finance_bff_web.service;

import cl.duoc.finance_bff_web.model.CuentaDTO;
import cl.duoc.finance_bff_web.model.EstadoFinancieroDTO;
import cl.duoc.finance_bff_web.model.ResumenWebDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementacion del servicio BFF Web.
 *
 * Orquesta las llamadas HTTP al microservicio finance-batch (puerto 8080)
 * y combina las respuestas de cuentas y transacciones en un unico DTO.
 *
 * Responsabilidades:
 * - Propagar el token JWT del cliente hacia el backend (token relay)
 * - Invocar los endpoints REST del backend usando RestTemplate
 * - Manejar errores de comunicacion (404, 403, conexion rechazada, etc.)
 * - Construir la respuesta unificada para el frontend web
 */
@Service
public class FinanceWebServiceImpl implements FinanceWebService {

    @Autowired
    private RestTemplate restTemplate;

    /** URL base del microservicio finance-batch */
    private final String BACKEND_URL = "http://localhost:8080/api/v1";

    /**
     * Construye los headers HTTP incluyendo el token JWT de la peticion actual.
     *
     * Extrae el header "Authorization" de la peticion entrante al BFF
     * y lo reenvía hacia el backend. Esto permite que el backend valide
     * la identidad del usuario sin requerir una nueva autenticacion.
     *
     * @return HttpHeaders con el token Bearer propagado (si existe)
     */
    private HttpHeaders getHeadersConToken() {
        HttpHeaders headers = new HttpHeaders();
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            String authHeader = attributes.getRequest().getHeader("Authorization");
            if (authHeader != null) {
                headers.set("Authorization", authHeader);
            }
        }
        return headers;
    }

    /**
     * {@inheritDoc}
     *
     * Flujo de ejecucion:
     * 1. Crea el ResumenWebDTO con la fecha de consulta actual
     * 2. Obtiene los headers con el token JWT propagado
     * 3. Llama a GET /api/v1/cuentas/{id} para obtener datos de la cuenta
     * 4. Llama a GET /api/v1/cuentas/{id}/transacciones para obtener movimientos
     * 5. Combina ambas respuestas en el DTO de salida
     *
     * Manejo de errores:
     * - 404 Not Found: La cuenta no existe
     * - 403 Forbidden: Token invalido o expirado en el backend
     * - ResourceAccessException: El backend no esta disponible
     * - Exception generica: Cualquier otro error inesperado
     */
    @Override
    public ResumenWebDTO obtenerResumenCuenta(Long id) {
        ResumenWebDTO resumen = new ResumenWebDTO();
        resumen.setFechaConsulta(LocalDateTime.now());

        try {
            // Preparar headers con el token JWT de la peticion original
            HttpEntity<String> entity = new HttpEntity<>(getHeadersConToken());

            // LLAMADA 1: Obtener datos de la cuenta desde finance-batch
            String urlCuenta = BACKEND_URL + "/cuentas/" + id;
            ResponseEntity<CuentaDTO> responseCuenta = restTemplate.exchange(
                urlCuenta,
                HttpMethod.GET,
                entity,
                CuentaDTO.class
            );
            resumen.setCuenta(responseCuenta.getBody());

            // LLAMADA 2: Obtener movimientos/transacciones de la cuenta
            String urlMovimientos = BACKEND_URL + "/cuentas/" + id + "/transacciones";
            ResponseEntity<List<EstadoFinancieroDTO>> responseMovimientos = restTemplate.exchange(
                urlMovimientos,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<EstadoFinancieroDTO>>() {}
            );

            resumen.setMovimientos(responseMovimientos.getBody());
            resumen.setMensaje("Consulta Exitosa - Cliente Web (Datos Completos)");

        } catch (HttpClientErrorException.NotFound e) {
            resumen.setMensaje("Error: La cuenta ID " + id + " no fue encontrada.");
        } catch (HttpClientErrorException.Forbidden e) {
            resumen.setMensaje("Error de Seguridad: Token inválido o expirado.");
        } catch (ResourceAccessException e) {
            resumen.setMensaje("Error Crítico: No se pudo conectar con Finance-Batch.");
        } catch (Exception e) {
            resumen.setMensaje("Error interno: " + e.getMessage());
        }

        return resumen;
    }
}
