package pe.edu.upeu.autoandes.service.impl;

import pe.edu.upeu.autoandes.dto.reporte.IngresoPorServicioDTO;
import pe.edu.upeu.autoandes.enums.EstadoOrdenServicio;
import pe.edu.upeu.autoandes.repository.OrdenServicioRepository;
import pe.edu.upeu.autoandes.exception.SolicitudInvalidaException;
import pe.edu.upeu.autoandes.service.service.ReporteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class ReporteServiceImpl implements ReporteService {

    private final OrdenServicioRepository ordenServicioRepository;

    public ReporteServiceImpl(OrdenServicioRepository ordenServicioRepository) {
        this.ordenServicioRepository = ordenServicioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<IngresoPorServicioDTO> ingresosPorServicio(LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new SolicitudInvalidaException("La fecha inicial no puede ser posterior a la fecha final");
        }
        LocalDateTime fechaDesde = desde == null ? null : desde.atStartOfDay();
        LocalDateTime fechaHasta = hasta == null ? null : hasta.atTime(LocalTime.MAX);

        return ordenServicioRepository.reporteIngresosPorServicio(
                EstadoOrdenServicio.REGISTRADA,
                fechaDesde,
                fechaHasta
        );
    }
}
