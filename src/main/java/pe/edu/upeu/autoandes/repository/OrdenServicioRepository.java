package pe.edu.upeu.autoandes.repository;

import pe.edu.upeu.autoandes.dto.reporte.IngresoPorServicioDTO;
import pe.edu.upeu.autoandes.entity.OrdenServicio;
import pe.edu.upeu.autoandes.enums.EstadoOrdenServicio;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrdenServicioRepository extends JpaRepository<OrdenServicio, Long> {

    boolean existsByVehiculoId(Long vehiculoId);

    Optional<OrdenServicio> findFirstByVehiculoIdAndEstadoOrderByFechaDescIdDesc(
            Long vehiculoId,
            EstadoOrdenServicio estado
    );

    @Query("""
            select distinct o
            from OrdenServicio o
            join fetch o.vehiculo v
            left join fetch o.detalles d
            left join fetch d.servicio s
            where (:placa is null or upper(v.placa) = upper(:placa))
              and (:estado is null or o.estado = :estado)
              and (:desde is null or o.fecha >= :desde)
              and (:hasta is null or o.fecha <= :hasta)
            """)
    List<OrdenServicio> buscar(
            @Param("placa") String placa,
            @Param("estado") EstadoOrdenServicio estado,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            Sort sort
    );

    @Query("""
            select distinct o
            from OrdenServicio o
            join fetch o.vehiculo v
            left join fetch o.detalles d
            left join fetch d.servicio s
            where o.id = :id
            """)
    Optional<OrdenServicio> buscarConDetallePorId(@Param("id") Long id);

    @Query("""
            select new pe.edu.upeu.autoandes.dto.reporte.IngresoPorServicioDTO(
                s.codigo,
                s.nombre,
                sum(d.cantidad),
                sum(d.subtotal)
            )
            from DetalleOrden d
            join d.orden o
            join d.servicio s
            where o.estado = :estadoRegistrada
              and (:desde is null or o.fecha >= :desde)
              and (:hasta is null or o.fecha <= :hasta)
            group by s.codigo, s.nombre
            order by sum(d.subtotal) desc
            """)
    List<IngresoPorServicioDTO> reporteIngresosPorServicio(
            @Param("estadoRegistrada") EstadoOrdenServicio estadoRegistrada,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta
    );
}
