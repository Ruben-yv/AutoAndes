package pe.edu.upeu.autoandes.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upeu.autoandes.enums.EstadoOrdenServicio;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OrdenServicioResponseDTO {
    private Long id;
    private String numero;
    private LocalDateTime fecha;
    private Long vehiculoId;
    private String placa;
    private Integer kilometraje;
    private EstadoOrdenServicio estado;
    private BigDecimal total;
    private String usuarioRegistro;
    private String motivoAnulacion;
    private List<DetalleOrdenResponseDTO> detalles;
}
