package cl.duoc.finance_bff_web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.finance_bff_web.model.ResumenWebDTO;
import cl.duoc.finance_bff_web.service.FinanceWebService;

@RestController
@RequestMapping("/bff/web/v1")
public class FinanceWebController {

    @Autowired
    private FinanceWebService financeWebService;

    @GetMapping("/cuentas/{id}")
    public ResponseEntity<ResumenWebDTO> obtenerResumenClienteWeb(@PathVariable Long id) {
        ResumenWebDTO respuesta = financeWebService.obtenerResumenCuenta(id);
        return ResponseEntity.ok(respuesta);
    }
}