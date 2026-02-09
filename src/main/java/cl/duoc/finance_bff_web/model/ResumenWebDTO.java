package cl.duoc.finance_bff_web.model;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

/**
 * DTO de respuesta principal del BFF Web.
 *
 * Combina los datos de cuenta y movimientos en una unica estructura
 * optimizada para el frontend web. Este es el objeto que se serializa
 * como JSON en la respuesta del endpoint /bff/web/v1/cuentas/{id}.
 *
 * Estructura de respuesta:
 * - mensaje: Mensaje de estado de la operacion (exito o descripcion del error)
 * - fechaConsulta: Timestamp del momento en que se realizo la consulta
 * - cuenta: Datos completos de la cuenta consultada
 * - movimientos: Lista de transacciones asociadas a la cuenta
 *
 * Ejemplo de respuesta exitosa:
 * {
 *   "mensaje": "Consulta Exitosa - Cliente Web (Datos Completos)",
 *   "fechaConsulta": "2026-02-09T17:00:00",
 *   "cuenta": { ... },
 *   "movimientos": [ ... ]
 * }
 */
@Data
public class ResumenWebDTO {
    /** Mensaje indicando el resultado de la operacion (exito o error) */
    private String mensaje;

    /** Fecha y hora en que se realizo la consulta al backend */
    private LocalDateTime fechaConsulta;

    /** Datos de la cuenta financiera consultada */
    private CuentaDTO cuenta;

    /** Lista de movimientos/transacciones de la cuenta */
    private List<EstadoFinancieroDTO> movimientos;
}
