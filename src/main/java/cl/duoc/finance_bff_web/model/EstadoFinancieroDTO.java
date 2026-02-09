package cl.duoc.finance_bff_web.model;

import java.time.LocalDate;

import lombok.Data;

/**
 * DTO que representa una transaccion o movimiento financiero.
 *
 * Mapea la respuesta JSON del microservicio finance-batch
 * (endpoint /api/v1/cuentas/{id}/transacciones).
 * Cada instancia representa un movimiento individual asociado a una cuenta.
 *
 * Campos:
 * - id: Identificador unico de la transaccion
 * - cuentaId: Referencia a la cuenta asociada
 * - fecha: Fecha en que se realizo la transaccion
 * - transaccion: Tipo de transaccion (ej: "deposito", "retiro", "transferencia")
 * - monto: Valor monetario de la transaccion
 * - descripcion: Detalle descriptivo del movimiento
 */
@Data
public class EstadoFinancieroDTO {
    private Long id;
    private Long cuentaId;
    private LocalDate fecha;
    private String transaccion;
    private Double monto;
    private String descripcion;
}
