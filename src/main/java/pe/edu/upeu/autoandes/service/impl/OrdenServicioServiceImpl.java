package pe.edu.upeu.autoandes.service.impl;

import pe.edu.upeu.autoandes.dto.AnularOrdenRequestDTO;
import pe.edu.upeu.autoandes.dto.DetalleOrdenRequestDTO;
import pe.edu.upeu.autoandes.dto.OrdenServicioRequestDTO;
import pe.edu.upeu.autoandes.dto.OrdenServicioResponseDTO;
import pe.edu.upeu.autoandes.entity.DetalleOrden;
import pe.edu.upeu.autoandes.entity.OrdenServicio;
import pe.edu.upeu.autoandes.entity.Servicio;
import pe.edu.upeu.autoandes.entity.Vehiculo;
import pe.edu.upeu.autoandes.enums.EstadoOrdenServicio;
import pe.edu.upeu.autoandes.exception.RecursoNoEncontradoException;
import pe.edu.upeu.autoandes.exception.ReglaNegocioException;
import pe.edu.upeu.autoandes.exception.SolicitudInvalidaException;
import pe.edu.upeu.autoandes.mapper.OrdenServicioMapper;
import pe.edu.upeu.autoandes.repository.OrdenServicioRepository;
import pe.edu.upeu.autoandes.repository.ServicioRepository;
import pe.edu.upeu.autoandes.repository.VehiculoRepository;
import pe.edu.upeu.autoandes.service.service.OrdenServicioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class OrdenServicioServiceImpl implements OrdenServicioService {
    private static final Logger log = LoggerFactory.getLogger(OrdenServicioServiceImpl.class);

    private final OrdenServicioRepository ordenServicioRepository;
    private final VehiculoRepository vehiculoRepository;
    private final ServicioRepository servicioRepository;
    private final OrdenServicioMapper ordenServicioMapper;

    public OrdenServicioServiceImpl(
            OrdenServicioRepository ordenServicioRepository,
            VehiculoRepository vehiculoRepository,
            ServicioRepository servicioRepository,
            OrdenServicioMapper ordenServicioMapper
    ) {
        this.ordenServicioRepository = ordenServicioRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.servicioRepository = servicioRepository;
        this.ordenServicioMapper = ordenServicioMapper;
    }

    @Override
    @Transactional
    public OrdenServicioResponseDTO registrar(OrdenServicioRequestDTO request, String usuarioRegistro) {
        validarSolicitud(request, usuarioRegistro);

        Vehiculo vehiculo = vehiculoRepository.findById(request.getVehiculoId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Vehículo no encontrado con id: " + request.getVehiculoId()
                ));

        Set<Long> serviciosIncluidos = new HashSet<>();
        List<DetalleOrden> detalles = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (DetalleOrdenRequestDTO detalleRequest : request.getDetalles()) {
            Long servicioId = detalleRequest.getServicioId();
            Integer cantidad = detalleRequest.getCantidad();

            if (!serviciosIncluidos.add(servicioId)) {
                log.warn("Registro de orden rechazado: servicio repetido id={}", servicioId);
                throw new SolicitudInvalidaException("No se puede repetir un servicio en la misma orden");
            }
            if (cantidad == null || cantidad < 1 || cantidad > 5) {
                log.warn("Registro de orden rechazado: cantidad inválida servicio={} cantidad={}",
                        servicioId, cantidad);
                throw new SolicitudInvalidaException("Cada detalle debe tener una cantidad entre 1 y 5");
            }

            Servicio servicio = servicioRepository.findById(servicioId)
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "Servicio no encontrado con id: " + servicioId
                    ));
            if (!Boolean.TRUE.equals(servicio.getActivo())) {
                log.warn("Registro de orden rechazado: servicio inactivo id={}", servicioId);
                throw new ReglaNegocioException("El servicio " + servicio.getCodigo() + " está inactivo");
            }

            BigDecimal subtotal = servicio.getPrecio().multiply(BigDecimal.valueOf(cantidad));
            DetalleOrden detalle = new DetalleOrden();
            detalle.setServicio(servicio);
            detalle.setCantidad(cantidad);
            detalle.setPrecioUnitario(servicio.getPrecio());
            detalle.setSubtotal(subtotal);
            detalles.add(detalle);
            total = total.add(subtotal);
        }

        ordenServicioRepository.findFirstByVehiculoIdAndEstadoOrderByFechaDescIdDesc(
                vehiculo.getId(), EstadoOrdenServicio.REGISTRADA
        ).ifPresent(ultimaOrden -> {
            if (request.getKilometraje() < ultimaOrden.getKilometraje()) {
                log.warn("Registro de orden rechazado: kilometraje={} menor al último={} para vehículo={}",
                        request.getKilometraje(), ultimaOrden.getKilometraje(), vehiculo.getId());
                throw new ReglaNegocioException(
                        "El kilometraje no puede ser menor al de la última orden registrada: "
                                + ultimaOrden.getKilometraje()
                );
            }
        });

        OrdenServicio orden = new OrdenServicio();
        orden.setNumero(generarNumero());
        orden.setFecha(LocalDateTime.now());
        orden.setVehiculo(vehiculo);
        orden.setKilometraje(request.getKilometraje());
        orden.setEstado(EstadoOrdenServicio.REGISTRADA);
        orden.setTotal(total);
        orden.setUsuarioRegistro(usuarioRegistro.trim());
        orden.setDetalles(detalles);
        detalles.forEach(detalle -> detalle.setOrden(orden));

        OrdenServicio guardada = ordenServicioRepository.save(orden);
        log.info("Orden de servicio registrada id={} numero={} total={}",
                guardada.getId(), guardada.getNumero(), guardada.getTotal());
        return ordenServicioMapper.toResponseDTO(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenServicioResponseDTO> listar(
            String placa,
            EstadoOrdenServicio estado,
            LocalDate desde,
            LocalDate hasta
    ) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new SolicitudInvalidaException("La fecha inicial no puede ser posterior a la fecha final");
        }

        String placaNormalizada = placa == null || placa.isBlank() ? null : placa.trim().toUpperCase();
        LocalDateTime fechaDesde = desde == null ? null : desde.atStartOfDay();
        LocalDateTime fechaHasta = hasta == null ? null : hasta.atTime(LocalTime.MAX);

        return ordenServicioRepository.buscar(
                        placaNormalizada,
                        estado,
                        fechaDesde,
                        fechaHasta,
                        Sort.by(Sort.Order.desc("fecha"), Sort.Order.desc("id"))
                )
                .stream()
                .map(ordenServicioMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrdenServicioResponseDTO buscarPorId(Long id) {
        OrdenServicio orden = ordenServicioRepository.buscarConDetallePorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Orden de servicio no encontrada con id: " + id
                ));
        return ordenServicioMapper.toResponseDTO(orden);
    }

    @Override
    @Transactional
    public OrdenServicioResponseDTO anular(Long id, AnularOrdenRequestDTO request) {
        OrdenServicio orden = ordenServicioRepository.buscarConDetallePorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Orden de servicio no encontrada con id: " + id
                ));

        if (orden.getEstado() != EstadoOrdenServicio.REGISTRADA) {
            log.warn("Anulación rechazada: orden no registrada id={} estado={}", id, orden.getEstado());
            throw new ReglaNegocioException("Solo se puede anular una orden en estado REGISTRADA");
        }
        if (!orden.getFecha().toLocalDate().equals(LocalDate.now())) {
            log.warn("Anulación rechazada: la orden no se registró hoy id={} fecha={}", id, orden.getFecha());
            throw new ReglaNegocioException("Solo se puede anular la orden el mismo día de su registro");
        }
        if (request == null || request.getMotivo() == null
                || request.getMotivo().trim().length() < 10
                || request.getMotivo().trim().length() > 200) {
            log.warn("Anulación rechazada: motivo inválido para orden id={}", id);
            throw new SolicitudInvalidaException("El motivo debe tener entre 10 y 200 caracteres");
        }

        orden.setEstado(EstadoOrdenServicio.ANULADA);
        orden.setMotivoAnulacion(request.getMotivo().trim());
        OrdenServicio anulada = ordenServicioRepository.save(orden);
        log.info("Orden de servicio anulada id={} numero={}", id, anulada.getNumero());
        return ordenServicioMapper.toResponseDTO(anulada);
    }

    private void validarSolicitud(OrdenServicioRequestDTO request, String usuarioRegistro) {
        if (request == null || request.getDetalles() == null
                || request.getDetalles().isEmpty() || request.getDetalles().size() > 10) {
            log.warn("Registro de orden rechazado: cantidad de detalles inválida");
            throw new SolicitudInvalidaException("La orden debe tener entre 1 y 10 detalles");
        }
        if (request.getKilometraje() == null || request.getKilometraje() < 0) {
            log.warn("Registro de orden rechazado: kilometraje inválido");
            throw new SolicitudInvalidaException("El kilometraje es obligatorio y no puede ser negativo");
        }
        if (usuarioRegistro == null || usuarioRegistro.isBlank() || usuarioRegistro.trim().length() > 30) {
            throw new ReglaNegocioException("El usuario que registra la orden no es válido");
        }
    }

    private String generarNumero() {
        long siguiente = ordenServicioRepository.count() + 1;
        if (siguiente > 999_999) {
            throw new ReglaNegocioException("Se alcanzó el máximo de órdenes numerables");
        }
        return "OS-%06d".formatted(siguiente);
    }
}
