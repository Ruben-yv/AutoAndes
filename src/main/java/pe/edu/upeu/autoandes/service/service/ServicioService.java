package pe.edu.upeu.autoandes.service.service;

import pe.edu.upeu.autoandes.dto.ServicioRequestDTO;
import pe.edu.upeu.autoandes.dto.ServicioResponseDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ServicioService {
    @Transactional(readOnly = true)
    List<ServicioResponseDTO> listar();

    @Transactional(readOnly = true)
    ServicioResponseDTO buscarPorId(Long id);

    ServicioResponseDTO registrar(ServicioRequestDTO request);

    ServicioResponseDTO actualizar(Long id, ServicioRequestDTO request);

    ServicioResponseDTO darDeBaja(Long id);

    void eliminar(Long id);
}
