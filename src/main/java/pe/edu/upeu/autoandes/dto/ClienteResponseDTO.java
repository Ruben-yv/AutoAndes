package pe.edu.upeu.autoandes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ClienteResponseDTO {
    private Long id;
    private String documento;
    private String nombres;
    private String telefono;
}
