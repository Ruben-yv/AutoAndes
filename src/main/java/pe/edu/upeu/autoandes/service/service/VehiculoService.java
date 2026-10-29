package pe.edu.upeu.autoandes.service.service;

import pe.edu.upeu.autoandes.dto.VehiculoRequestDTO;
import pe.edu.upeu.autoandes.dto.VehiculoResponseDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface VehiculoService {
    @Transactional(readOnly = true)
    List<VehiculoResponseDTO> listar(Long clienteId);

    @Transactional(readOnly = true)
    VehiculoResponseDTO buscarPorId(Long id);

    VehiculoResponseDTO registrar(VehiculoRequestDTO request);

    VehiculoResponseDTO actualizar(Long id, VehiculoRequestDTO request);

    void eliminar(Long id);
}
