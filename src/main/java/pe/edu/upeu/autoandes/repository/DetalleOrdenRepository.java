package pe.edu.upeu.autoandes.repository;

import pe.edu.upeu.autoandes.entity.DetalleOrden;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetalleOrdenRepository extends JpaRepository<DetalleOrden, Long> {
    boolean existsByServicio_Id(Long servicioId);
}
