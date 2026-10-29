package pe.edu.upeu.autoandes.service.impl;

import pe.edu.upeu.autoandes.dto.ServicioRequestDTO;
import pe.edu.upeu.autoandes.dto.ServicioResponseDTO;
import pe.edu.upeu.autoandes.entity.Servicio;
import pe.edu.upeu.autoandes.exception.RecursoNoEncontradoException;
import pe.edu.upeu.autoandes.exception.ReglaNegocioException;
import pe.edu.upeu.autoandes.mapper.ServicioMapper;
import pe.edu.upeu.autoandes.repository.DetalleOrdenRepository;
import pe.edu.upeu.autoandes.repository.ServicioRepository;
import pe.edu.upeu.autoandes.service.service.ServicioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServicioServiceImpl implements ServicioService {
    private static final Logger log = LoggerFactory.getLogger(ServicioServiceImpl.class);

    private final ServicioRepository servicioRepository;
    private final DetalleOrdenRepository detalleOrdenRepository;
    private final ServicioMapper servicioMapper;

    public ServicioServiceImpl(
            ServicioRepository servicioRepository,
            DetalleOrdenRepository detalleOrdenRepository,
            ServicioMapper servicioMapper
    ) {
        this.servicioRepository = servicioRepository;
        this.detalleOrdenRepository = detalleOrdenRepository;
        this.servicioMapper = servicioMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServicioResponseDTO> listar() {
        log.info("Listando servicios");
        return servicioRepository.findAll(Sort.by(Sort.Direction.ASC, "nombre"))
                .stream()
                .map(servicioMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ServicioResponseDTO buscarPorId(Long id) {
        return servicioMapper.toResponseDTO(obtenerEntidad(id));
    }

    @Override
    @Transactional
    public ServicioResponseDTO registrar(ServicioRequestDTO request) {
        String codigo = normalizarCodigo(request.getCodigo());
        if (servicioRepository.existsByCodigoIgnoreCase(codigo)) {
            log.warn("Alta de servicio rechazada: código duplicado={}", codigo);
            throw new ReglaNegocioException("Ya existe un servicio con el código: " + codigo);
        }

        Servicio servicio = servicioMapper.toEntity(request);
        servicio.setCodigo(codigo);
        if (servicio.getActivo() == null) {
            servicio.setActivo(true);
        }

        Servicio guardado = servicioRepository.save(servicio);
        log.info("Servicio registrado id={} codigo={}", guardado.getId(), guardado.getCodigo());
        return servicioMapper.toResponseDTO(guardado);
    }

    @Override
    @Transactional
    public ServicioResponseDTO actualizar(Long id, ServicioRequestDTO request) {
        Servicio servicio = obtenerEntidad(id);
        String codigo = normalizarCodigo(request.getCodigo());
        if (servicioRepository.existsByCodigoIgnoreCaseAndIdNot(codigo, id)) {
            log.warn("Actualización de servicio rechazada: código duplicado={} id={}", codigo, id);
            throw new ReglaNegocioException("Ya existe otro servicio con el código: " + codigo);
        }

        servicioMapper.updateEntity(servicio, request);
        servicio.setCodigo(codigo);
        Servicio actualizado = servicioRepository.save(servicio);
        log.info("Servicio actualizado id={} codigo={}", actualizado.getId(), actualizado.getCodigo());
        return servicioMapper.toResponseDTO(actualizado);
    }

    @Override
    @Transactional
    public ServicioResponseDTO darDeBaja(Long id) {
        Servicio servicio = obtenerEntidad(id);
        servicio.setActivo(false);
        Servicio actualizado = servicioRepository.save(servicio);
        log.info("Servicio dado de baja id={} codigo={}", id, servicio.getCodigo());
        return servicioMapper.toResponseDTO(actualizado);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Servicio servicio = obtenerEntidad(id);
        if (detalleOrdenRepository.existsByServicio_Id(id)) {
            log.warn("Eliminación física de servicio rechazada: está en uso id={}", id);
            throw new ReglaNegocioException(
                    "No se puede eliminar el servicio porque ya aparece en una orden de servicio"
            );
        }
        servicioRepository.delete(servicio);
        log.info("Servicio eliminado físicamente id={}", id);
    }

    private Servicio obtenerEntidad(Long id) {
        return servicioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Servicio no encontrado con id: " + id));
    }

    private String normalizarCodigo(String codigo) {
        return codigo.trim().toUpperCase();
    }
}
