package cl.duoc.finance_bff_web.service;

import cl.duoc.finance_bff_web.model.CuentaDTO;
import cl.duoc.finance_bff_web.model.EstadoFinancieroDTO;
import cl.duoc.finance_bff_web.model.ResumenWebDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class FinanceWebServiceImpl implements FinanceWebService {

    @Autowired
    private RestTemplate restTemplate;

    // INYECTAMOS LA UTILIDAD PARA FABRICAR TOKENS (Faltaba en este BFF)
    @Autowired
    private cl.duoc.finance_bff_web.security.JwtUtil jwtUtil;

    private final String BACKEND_URL = "http://localhost:8080/api/v1";

    /**
     * Extrae el token si viene de Postman, o fabrica uno interno si viene de GitHub
     */
    private HttpHeaders getHeadersConToken() {
        HttpHeaders headers = new HttpHeaders();
        try {
            // 1. Intentar sacar el token si la petición viene desde Postman (Header explícito)
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null && attributes.getRequest().getHeader("Authorization") != null) {
                headers.set("Authorization", attributes.getRequest().getHeader("Authorization"));
                return headers;
            }

            // 2. Si venimos desde el navegador (GitHub OAuth2), fabricamos el pase VIP
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof org.springframework.security.oauth2.core.user.OAuth2User) {
                // Fabricamos un JWT válido para el Core usando los roles del Web
                String tokenInterno = jwtUtil.generateToken("usuario_web", "ROLE_CLIENTE_WEB");
                headers.set("Authorization", "Bearer " + tokenInterno);
            }
        } catch (Exception e) {
            System.err.println("Error generando token relay: " + e.getMessage());
        }
        return headers;
    }

    @Override
    @CircuitBreaker(name = "financeCore", fallbackMethod = "fallbackObtenerResumenCuenta")
    public ResumenWebDTO obtenerResumenCuenta(Long id) {
        ResumenWebDTO resumen = new ResumenWebDTO();
        resumen.setFechaConsulta(LocalDateTime.now());
        HttpEntity<String> entity = new HttpEntity<>(getHeadersConToken());

        try {
            // LLAMADA 1: Obtener datos de la cuenta
            String urlCuenta = BACKEND_URL + "/cuentas/" + id;
            ResponseEntity<CuentaDTO> responseCuenta = restTemplate.exchange(
                urlCuenta, HttpMethod.GET, entity, CuentaDTO.class
            );
            resumen.setCuenta(responseCuenta.getBody());

            // LLAMADA 2: Obtener movimientos
            String urlMovimientos = BACKEND_URL + "/cuentas/" + id + "/transacciones";
            ResponseEntity<List<EstadoFinancieroDTO>> responseMovimientos = restTemplate.exchange(
                urlMovimientos, HttpMethod.GET, entity, new ParameterizedTypeReference<List<EstadoFinancieroDTO>>() {}
            );

            resumen.setMovimientos(responseMovimientos.getBody());
            resumen.setMensaje("Consulta Exitosa - Cliente Web (Datos Completos)");

        } catch (HttpClientErrorException e) {
            // Manejamos los errores limpios del Core sin activar el cortacircuitos
            if (e.getStatusCode().value() == 404) {
                resumen.setMensaje("Aviso: La cuenta ID " + id + " no fue encontrada.");
            } else if (e.getStatusCode().value() == 403) {
                resumen.setMensaje("Aviso: No tiene permisos (Token inválido o expirado).");
            } else {
                resumen.setMensaje("Error en la petición: " + e.getMessage());
            }
        }

        return resumen;
    }

    /**
     * MÉTODO DE FALLBACK (Se activa si el Core se apaga o hay Timeout)
     */
    public ResumenWebDTO fallbackObtenerResumenCuenta(Long id, Throwable t) {
        System.err.println("¡Circuit Breaker activado en BFF Web! Falló la comunicación: " + t.getMessage());
        
        ResumenWebDTO resumenFallback = new ResumenWebDTO();
        resumenFallback.setFechaConsulta(LocalDateTime.now());
        resumenFallback.setMensaje("Servicios web temporalmente no disponibles. Por favor, intente más tarde.");
        resumenFallback.setCuenta(null);
        resumenFallback.setMovimientos(Collections.emptyList());
        
        return resumenFallback;
    }
}