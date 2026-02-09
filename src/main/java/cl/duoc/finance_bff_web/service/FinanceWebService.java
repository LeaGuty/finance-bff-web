package cl.duoc.finance_bff_web.service;

import cl.duoc.finance_bff_web.model.ResumenWebDTO;

/**
 * Interfaz de servicio para las operaciones del BFF Web.
 *
 * Define el contrato para la capa de servicio que se encarga de orquestar
 * las llamadas al microservicio backend (finance-batch) y combinar
 * las respuestas en un formato adecuado para el cliente web.
 *
 * Implementacion: {@link FinanceWebServiceImpl}
 */
public interface FinanceWebService {

    /**
     * Obtiene el resumen financiero completo de una cuenta.
     *
     * Realiza dos llamadas al backend (finance-batch):
     * 1. GET /api/v1/cuentas/{id} - Datos de la cuenta
     * 2. GET /api/v1/cuentas/{id}/transacciones - Movimientos de la cuenta
     *
     * Combina ambas respuestas en un unico ResumenWebDTO.
     *
     * @param id Identificador de la cuenta a consultar
     * @return ResumenWebDTO con los datos combinados y mensaje de estado
     */
    ResumenWebDTO obtenerResumenCuenta(Long id);
}
