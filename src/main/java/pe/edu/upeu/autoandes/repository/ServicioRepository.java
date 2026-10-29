package pe.edu.upeu.autoandes.repository;

import pe.edu.upeu.autoandes.entity.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServicioRepository extends JpaRepository<Servicio, Long> {
    boolean existsByCodigoIgnoreCase(String codigo);
    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);
    List<Servicio> findByActivoTrueOrderByNombreAsc();
    List<Servicio> findByNombreContainingIgnoreCaseOrderByNombreAsc(String nombre);
    Optional<Servicio> findByCodigoIgnoreCase(String codigo);
}
