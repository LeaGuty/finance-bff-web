package cl.duoc.finance_bff_web.model;

import lombok.Data; // Si no usas Lombok, genera Getters/Setters manualmente

@Data
public class CuentaDTO {
    private Long id;
    private Long cuentaId;
    private String nombre;
    private Double saldo;
    private Integer edad;
    private String tipo;
    // Agregamos este campo por si el batch ya calculó intereses
    private Double interesAplicado; 
}