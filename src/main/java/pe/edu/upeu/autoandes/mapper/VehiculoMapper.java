package pe.edu.upeu.autoandes.mapper;

import pe.edu.upeu.autoandes.dto.VehiculoResponseDTO;
import pe.edu.upeu.autoandes.entity.Vehiculo;
import org.springframework.stereotype.Component;

@Component
public class VehiculoMapper {
    public VehiculoResponseDTO toResponseDTO(Vehiculo vehiculo) {
        return new VehiculoResponseDTO(
                vehiculo.getId(),
                vehiculo.getPlaca(),
                vehiculo.getMarca(),
                vehiculo.getModelo(),
                vehiculo.getAnio(),
                vehiculo.getCliente().getId(),
                vehiculo.getCliente().getNombres()
        );
    }
}
