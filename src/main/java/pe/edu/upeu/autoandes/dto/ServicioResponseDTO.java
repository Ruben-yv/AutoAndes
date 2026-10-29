package pe.edu.upeu.autoandes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ServicioResponseDTO {
    private Long id;
    private String codigo;
    private String nombre;
    private BigDecimal precio;
    private Integer duracionMin;
    private Boolean activo;
}
