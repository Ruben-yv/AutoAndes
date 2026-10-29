package pe.edu.upeu.autoandes.mapper;

import pe.edu.upeu.autoandes.dto.ServicioRequestDTO;
import pe.edu.upeu.autoandes.dto.ServicioResponseDTO;
import pe.edu.upeu.autoandes.entity.Servicio;
import org.springframework.stereotype.Component;

@Component
public class ServicioMapper {
    public Servicio toEntity(ServicioRequestDTO request) {
        Servicio servicio = new Servicio();
        updateEntity(servicio, request);
        return servicio;
    }

    public void updateEntity(Servicio servicio, ServicioRequestDTO request) {
        servicio.setCodigo(request.getCodigo().trim().toUpperCase());
        servicio.setNombre(request.getNombre().trim());
        servicio.setPrecio(request.getPrecio());
        servicio.setDuracionMin(request.getDuracionMin());
    }

    public ServicioResponseDTO toResponseDTO(Servicio servicio) {
        return new ServicioResponseDTO(
                servicio.getId(),
                servicio.getCodigo(),
                servicio.getNombre(),
                servicio.getPrecio(),
                servicio.getDuracionMin(),
                servicio.getActivo()
        );
    }
}
