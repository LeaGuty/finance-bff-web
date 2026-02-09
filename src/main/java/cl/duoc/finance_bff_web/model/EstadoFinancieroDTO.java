package cl.duoc.finance_bff_web.model;

import java.time.LocalDate;

import lombok.Data;

@Data
public class EstadoFinancieroDTO {
    private Long id;
    private Long cuentaId;
    private LocalDate fecha;
    private String transaccion;
    private Double monto;
    private String descripcion;
}