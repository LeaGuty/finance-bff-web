package cl.duoc.finance_bff_web.model;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class ResumenWebDTO {
    // Metadatos para el Frontend
    private String mensaje;
    private LocalDateTime fechaConsulta;
    
    // Los datos del negocio combinados
    private CuentaDTO cuenta;
    private List<EstadoFinancieroDTO> movimientos;
}