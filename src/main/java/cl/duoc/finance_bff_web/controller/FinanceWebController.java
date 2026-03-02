package cl.duoc.finance_bff_web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.finance_bff_web.model.ResumenWebDTO;
import cl.duoc.finance_bff_web.service.FinanceWebService;

// IMPORTANTE: Aquí importamos el productor que acabas de crear
import cl.duoc.finance_bff_web.kafka.AuditoriaProducer;

@RestController
@RequestMapping("/bff/web/v1")
public class FinanceWebController {

    @Autowired
    private FinanceWebService financeWebService;

    // 1. INYECTAMOS EL NUEVO PRODUCTOR DE KAFKA
    @Autowired
    private AuditoriaProducer auditoriaProducer;

    @GetMapping("/cuentas/{id}")
    public ResponseEntity<ResumenWebDTO> obtenerResumenClienteWeb(@PathVariable Long id) {
        
        // Obtiene los datos como siempre (llamando al Core)
        ResumenWebDTO respuesta = financeWebService.obtenerResumenCuenta(id);
        
        // 2. ¡NUEVO! Dispara el mensaje a Kafka de forma asíncrona
        // Convertimos el Long a String para enviarlo en el mensaje
        auditoriaProducer.registrarConsulta(String.valueOf(id), "BFF-WEB");
        
        // Retorna la respuesta al cliente sin demoras
        return ResponseEntity.ok(respuesta);
    }
}