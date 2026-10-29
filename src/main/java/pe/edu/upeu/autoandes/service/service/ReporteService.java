package pe.edu.upeu.autoandes.service.service;

import pe.edu.upeu.autoandes.dto.reporte.IngresoPorServicioDTO;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

public interface ReporteService {

    @Transactional(readOnly = true)
    List<IngresoPorServicioDTO> ingresosPorServicio(LocalDate desde, LocalDate hasta);
}
