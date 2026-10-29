package pe.edu.upeu.autoandes.service.service;

import pe.edu.upeu.autoandes.dto.ClienteResponseDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ClienteService {
    @Transactional(readOnly = true)
    List<ClienteResponseDTO> listar();
}
