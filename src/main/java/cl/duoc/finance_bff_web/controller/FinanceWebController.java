package cl.duoc.finance_bff_web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.finance_bff_web.model.ResumenWebDTO;
import cl.duoc.finance_bff_web.service.FinanceWebService;

/**
 * Controlador REST principal del BFF Web.
 *
 * Expone los endpoints protegidos para clientes web bajo la ruta /bff/web/v1.
 * Requiere autenticacion JWT con rol CLIENTE_WEB (configurado en SecurityConfig).
 *
 * Endpoints:
 * - GET /bff/web/v1/cuentas/{id} - Obtiene resumen completo de una cuenta
 *
 * Ejemplo de uso con curl:
 *   curl -k -H "Authorization: Bearer {token}" https://localhost:8081/bff/web/v1/cuentas/1
 */
@RestController
@RequestMapping("/bff/web/v1")
public class FinanceWebController {

    @Autowired
    private FinanceWebService financeWebService;

    /**
     * Obtiene el resumen financiero completo de una cuenta para el cliente web.
     *
     * Combina datos de cuenta y transacciones en una unica respuesta.
     * La autenticacion y autorizacion se validan en el JwtFilter antes
     * de llegar a este metodo.
     *
     * @param id Identificador de la cuenta a consultar
     * @return ResponseEntity con ResumenWebDTO conteniendo cuenta y movimientos
     */
    @GetMapping("/cuentas/{id}")
    public ResponseEntity<ResumenWebDTO> obtenerResumenClienteWeb(@PathVariable Long id) {
        ResumenWebDTO respuesta = financeWebService.obtenerResumenCuenta(id);
        return ResponseEntity.ok(respuesta);
    }
}
