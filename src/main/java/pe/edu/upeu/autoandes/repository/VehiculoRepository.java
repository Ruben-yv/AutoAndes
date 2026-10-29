package pe.edu.upeu.autoandes.repository;

import pe.edu.upeu.autoandes.entity.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {
    boolean existsByPlacaIgnoreCase(String placa);
    boolean existsByPlacaIgnoreCaseAndIdNot(String placa, Long id);
    List<Vehiculo> findByClienteIdOrderByPlacaAsc(Long clienteId);
    Optional<Vehiculo> findByPlacaIgnoreCase(String placa);
}
