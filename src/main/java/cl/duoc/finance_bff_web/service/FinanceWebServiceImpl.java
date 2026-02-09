package cl.duoc.finance_bff_web.service;

import cl.duoc.finance_bff_web.model.CuentaDTO;
import cl.duoc.finance_bff_web.model.EstadoFinancieroDTO;
import cl.duoc.finance_bff_web.model.ResumenWebDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity; // <--- NUEVO
import org.springframework.http.HttpHeaders; // <--- NUEVO
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder; // <--- NUEVO
import org.springframework.web.context.request.ServletRequestAttributes; // <--- NUEVO

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class FinanceWebServiceImpl implements FinanceWebService {

    @Autowired
    private RestTemplate restTemplate;

    private final String BACKEND_URL = "http://localhost:8080/api/v1";

    // --- NUEVO MÉTODO AUXILIAR: Obtener el token de la petición actual ---
    private HttpHeaders getHeadersConToken() {
        HttpHeaders headers = new HttpHeaders();
        // Obtenemos la petición HTTP que llegó al BFF
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            // Extraemos el header "Authorization" original (el que trae el Bearer token)
            String authHeader = attributes.getRequest().getHeader("Authorization");
            if (authHeader != null) {
                headers.set("Authorization", authHeader);
            }
        }
        return headers;
    }

    @Override
    public ResumenWebDTO obtenerResumenCuenta(Long id) {
        ResumenWebDTO resumen = new ResumenWebDTO();
        resumen.setFechaConsulta(LocalDateTime.now());

        try {
            // Preparamos la entidad HTTP con los headers (el token)
            HttpEntity<String> entity = new HttpEntity<>(getHeadersConToken());

            // LLAMADA 1: Obtener datos de la Cuenta (CON TOKEN)
            String urlCuenta = BACKEND_URL + "/cuentas/" + id;
            
            // Usamos exchange en lugar de getForObject para poder enviar 'entity'
            ResponseEntity<CuentaDTO> responseCuenta = restTemplate.exchange(
                urlCuenta, 
                HttpMethod.GET, 
                entity, 
                CuentaDTO.class
            );
            resumen.setCuenta(responseCuenta.getBody());

            // LLAMADA 2: Obtener movimientos (CON TOKEN)
            String urlMovimientos = BACKEND_URL + "/cuentas/" + id + "/transacciones";
            
            ResponseEntity<List<EstadoFinancieroDTO>> responseMovimientos = restTemplate.exchange(
                urlMovimientos,
                HttpMethod.GET,
                entity, // <--- Aquí va el token
                new ParameterizedTypeReference<List<EstadoFinancieroDTO>>() {}
            );
            
            resumen.setMovimientos(responseMovimientos.getBody());
            resumen.setMensaje("Consulta Exitosa - Cliente Web (Datos Completos)");

        } catch (HttpClientErrorException.NotFound e) {
            resumen.setMensaje("Error: La cuenta ID " + id + " no fue encontrada.");
        } catch (HttpClientErrorException.Forbidden e) { // <--- Capturamos error de seguridad
            resumen.setMensaje("Error de Seguridad: Token inválido o expirado.");
        } catch (ResourceAccessException e) {
            resumen.setMensaje("Error Crítico: No se pudo conectar con Finance-Batch.");
        } catch (Exception e) {
            resumen.setMensaje("Error interno: " + e.getMessage());
        }

        return resumen;
    }
}