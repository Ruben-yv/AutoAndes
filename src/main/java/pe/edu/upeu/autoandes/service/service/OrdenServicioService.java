package pe.edu.upeu.autoandes.service.service;

import pe.edu.upeu.autoandes.dto.AnularOrdenRequestDTO;
import pe.edu.upeu.autoandes.dto.OrdenServicioRequestDTO;
import pe.edu.upeu.autoandes.dto.OrdenServicioResponseDTO;
import pe.edu.upeu.autoandes.enums.EstadoOrdenServicio;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

public interface OrdenServicioService {

    OrdenServicioResponseDTO registrar(OrdenServicioRequestDTO request, String usuarioRegistro);

    @Transactional(readOnly = true)
    List<OrdenServicioResponseDTO> listar(
            String placa,
            EstadoOrdenServicio estado,
            LocalDate desde,
            LocalDate hasta
    );

    @Transactional(readOnly = true)
    OrdenServicioResponseDTO buscarPorId(Long id);

    OrdenServicioResponseDTO anular(Long id, AnularOrdenRequestDTO request);
}
