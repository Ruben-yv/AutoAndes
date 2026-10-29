package pe.edu.upeu.autoandes.service.impl;

import pe.edu.upeu.autoandes.dto.VehiculoRequestDTO;
import pe.edu.upeu.autoandes.dto.VehiculoResponseDTO;
import pe.edu.upeu.autoandes.entity.Cliente;
import pe.edu.upeu.autoandes.entity.Vehiculo;
import pe.edu.upeu.autoandes.exception.RecursoNoEncontradoException;
import pe.edu.upeu.autoandes.exception.ReglaNegocioException;
import pe.edu.upeu.autoandes.mapper.VehiculoMapper;
import pe.edu.upeu.autoandes.repository.ClienteRepository;
import pe.edu.upeu.autoandes.repository.OrdenServicioRepository;
import pe.edu.upeu.autoandes.repository.VehiculoRepository;
import pe.edu.upeu.autoandes.service.service.VehiculoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VehiculoServiceImpl implements VehiculoService {
    private static final Logger log = LoggerFactory.getLogger(VehiculoServiceImpl.class);

    private final VehiculoRepository vehiculoRepository;
    private final ClienteRepository clienteRepository;
    private final OrdenServicioRepository ordenServicioRepository;
    private final VehiculoMapper vehiculoMapper;

    public VehiculoServiceImpl(
            VehiculoRepository vehiculoRepository,
            ClienteRepository clienteRepository,
            OrdenServicioRepository ordenServicioRepository,
            VehiculoMapper vehiculoMapper
    ) {
        this.vehiculoRepository = vehiculoRepository;
        this.clienteRepository = clienteRepository;
        this.ordenServicioRepository = ordenServicioRepository;
        this.vehiculoMapper = vehiculoMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculoResponseDTO> listar(Long clienteId) {
        List<Vehiculo> vehiculos;
        if (clienteId == null) {
            log.info("Listando todos los vehículos");
            vehiculos = vehiculoRepository.findAll(Sort.by(Sort.Direction.ASC, "placa"));
        } else {
            obtenerCliente(clienteId);
            log.info("Listando vehículos por cliente id={}", clienteId);
            vehiculos = vehiculoRepository.findByClienteIdOrderByPlacaAsc(clienteId);
        }
        return vehiculos.stream().map(vehiculoMapper::toResponseDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VehiculoResponseDTO buscarPorId(Long id) {
        return vehiculoMapper.toResponseDTO(obtenerEntidad(id));
    }

    @Override
    @Transactional
    public VehiculoResponseDTO registrar(VehiculoRequestDTO request) {
        String placa = normalizarPlaca(request.getPlaca());
        if (vehiculoRepository.existsByPlacaIgnoreCase(placa)) {
            log.warn("Alta de vehículo rechazada: placa duplicada={}", placa);
            throw new ReglaNegocioException("Ya existe un vehículo con la placa: " + placa);
        }

        Cliente cliente = obtenerCliente(request.getClienteId());
        Vehiculo vehiculo = new Vehiculo();
        aplicarRequest(vehiculo, request, placa, cliente);
        Vehiculo guardado = vehiculoRepository.save(vehiculo);
        log.info("Vehículo registrado id={} placa={}", guardado.getId(), guardado.getPlaca());
        return vehiculoMapper.toResponseDTO(guardado);
    }

    @Override
    @Transactional
    public VehiculoResponseDTO actualizar(Long id, VehiculoRequestDTO request) {
        Vehiculo vehiculo = obtenerEntidad(id);
        String placa = normalizarPlaca(request.getPlaca());
        if (vehiculoRepository.existsByPlacaIgnoreCaseAndIdNot(placa, id)) {
            log.warn("Actualización de vehículo rechazada: placa duplicada={} id={}", placa, id);
            throw new ReglaNegocioException("Ya existe otro vehículo con la placa: " + placa);
        }

        Cliente cliente = obtenerCliente(request.getClienteId());
        aplicarRequest(vehiculo, request, placa, cliente);
        Vehiculo actualizado = vehiculoRepository.save(vehiculo);
        log.info("Vehículo actualizado id={} placa={}", id, actualizado.getPlaca());
        return vehiculoMapper.toResponseDTO(actualizado);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Vehiculo vehiculo = obtenerEntidad(id);
        if (ordenServicioRepository.existsByVehiculoId(id)) {
            log.warn("Eliminación de vehículo rechazada: tiene órdenes registradas id={}", id);
            throw new ReglaNegocioException(
                    "No se puede eliminar el vehículo porque tiene órdenes de servicio registradas"
            );
        }
        vehiculoRepository.delete(vehiculo);
        log.info("Vehículo eliminado id={} placa={}", id, vehiculo.getPlaca());
    }

    private Vehiculo obtenerEntidad(Long id) {
        return vehiculoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Vehículo no encontrado con id: " + id));
    }

    private Cliente obtenerCliente(Long clienteId) {
        return clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado con id: " + clienteId));
    }

    private void aplicarRequest(Vehiculo vehiculo, VehiculoRequestDTO request, String placa, Cliente cliente) {
        vehiculo.setPlaca(placa);
        vehiculo.setMarca(request.getMarca().trim());
        vehiculo.setModelo(request.getModelo().trim());
        vehiculo.setAnio(request.getAnio());
        vehiculo.setCliente(cliente);
    }

    private String normalizarPlaca(String placa) {
        return placa.trim().toUpperCase();
    }
}
