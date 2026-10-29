package pe.edu.upeu.autoandes.mapper;

import pe.edu.upeu.autoandes.dto.DetalleOrdenResponseDTO;
import pe.edu.upeu.autoandes.dto.OrdenServicioResponseDTO;
import pe.edu.upeu.autoandes.entity.DetalleOrden;
import pe.edu.upeu.autoandes.entity.OrdenServicio;
import org.springframework.stereotype.Component;

@Component
public class OrdenServicioMapper {

    public OrdenServicioResponseDTO toResponseDTO(OrdenServicio orden) {
        return new OrdenServicioResponseDTO(
                orden.getId(),
                orden.getNumero(),
                orden.getFecha(),
                orden.getVehiculo().getId(),
                orden.getVehiculo().getPlaca(),
                orden.getKilometraje(),
                orden.getEstado(),
                orden.getTotal(),
                orden.getUsuarioRegistro(),
                orden.getMotivoAnulacion(),
                orden.getDetalles().stream().map(this::toDetalleResponseDTO).toList()
        );
    }

    private DetalleOrdenResponseDTO toDetalleResponseDTO(DetalleOrden detalle) {
        return new DetalleOrdenResponseDTO(
                detalle.getId(),
                detalle.getServicio().getId(),
                detalle.getServicio().getCodigo(),
                detalle.getServicio().getNombre(),
                detalle.getCantidad(),
                detalle.getPrecioUnitario(),
                detalle.getSubtotal()
        );
    }
}
