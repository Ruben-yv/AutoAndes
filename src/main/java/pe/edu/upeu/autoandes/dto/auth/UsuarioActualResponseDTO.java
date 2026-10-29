package pe.edu.upeu.autoandes.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UsuarioActualResponseDTO {
    private String username;
    private String nombreCompleto;
    private List<String> roles;
    private List<String> permisos;
}
