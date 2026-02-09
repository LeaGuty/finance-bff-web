package cl.duoc.finance_bff_web.service;

import cl.duoc.finance_bff_web.model.CuentaDTO;
import cl.duoc.finance_bff_web.model.EstadoFinancieroDTO;
import cl.duoc.finance_bff_web.model.ResumenWebDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class FinanceWebServiceImpl implements FinanceWebService {

    @Autowired
    private RestTemplate restTemplate;

    // URL del Backend Finance-Batch (Core)
    private final String BACKEND_URL = "http://localhost:8080/api/v1";

    @Override
    public ResumenWebDTO obtenerResumenCuenta(Long id) {
        ResumenWebDTO resumen = new ResumenWebDTO();
        resumen.setFechaConsulta(LocalDateTime.now());

        try {
            // LLAMADA 1: Obtener datos de la Cuenta
            // GET http://localhost:8080/api/v1/cuentas/{id}
            String urlCuenta = BACKEND_URL + "/cuentas/" + id;
            CuentaDTO cuenta = restTemplate.getForObject(urlCuenta, CuentaDTO.class);
            resumen.setCuenta(cuenta);

            // LLAMADA 2: Obtener movimientos (Transacciones)
            // GET http://localhost:8080/api/v1/cuentas/{id}/transacciones
            String urlMovimientos = BACKEND_URL + "/cuentas/" + id + "/transacciones";
            
            ResponseEntity<List<EstadoFinancieroDTO>> responseMovimientos = restTemplate.exchange(
                urlMovimientos,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<EstadoFinancieroDTO>>() {}
            );
            
            resumen.setMovimientos(responseMovimientos.getBody());
            resumen.setMensaje("Consulta Exitosa - Cliente Web (Datos Completos)");

        } catch (HttpClientErrorException.NotFound e) {
            // Manejo de error 404 si la cuenta no existe
            resumen.setMensaje("Error: La cuenta ID " + id + " no fue encontrada en el sistema Core.");
            resumen.setCuenta(null);
            resumen.setMovimientos(Collections.emptyList());
        } catch (ResourceAccessException e) {
            // Manejo de error si el Backend Finance-Batch está apagado
            resumen.setMensaje("Error Crítico: No se pudo conectar con el sistema Core Finance-Batch. Verifique que esté encendido.");
            resumen.setMovimientos(Collections.emptyList());
        } catch (Exception e) {
            // Otros errores
            resumen.setMensaje("Error interno: " + e.getMessage());
            resumen.setMovimientos(Collections.emptyList());
        }

        return resumen;
    }
}   