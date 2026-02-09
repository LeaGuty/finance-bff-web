package cl.duoc.finance_bff_web.model;

import lombok.Data;

/**
 * DTO (Data Transfer Object) que representa una cuenta financiera.
 *
 * Mapea la respuesta JSON del microservicio finance-batch (endpoint /api/v1/cuentas/{id}).
 * Se utiliza para transportar los datos de cuenta entre las capas del BFF
 * sin exponer la entidad de dominio del backend.
 *
 * Campos:
 * - id: Identificador interno del registro
 * - cuentaId: Identificador unico de la cuenta en el sistema financiero
 * - nombre: Nombre del titular de la cuenta
 * - saldo: Saldo actual disponible en la cuenta
 * - edad: Edad del titular
 * - tipo: Tipo de cuenta (ej: "ahorro", "corriente")
 * - interesAplicado: Interes calculado por el proceso batch (puede ser null si no se ha ejecutado)
 */
@Data
public class CuentaDTO {
    private Long id;
    private Long cuentaId;
    private String nombre;
    private Double saldo;
    private Integer edad;
    private String tipo;
    private Double interesAplicado;
}
