package pe.valuo.msprediccion.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.valuo.msprediccion.domain.model.estimacion.Estimacion;
import pe.valuo.msprediccion.domain.model.estimacion.TipoOperacion;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface SpringDataEstimacionRepository extends JpaRepository<Estimacion, UUID> {

    Optional<Estimacion> findByIdAndAgenteId(UUID id, UUID agenteId);

    Page<Estimacion> findByAgenteId(UUID agenteId, Pageable pageable);

    Page<Estimacion> findByAgenteIdAndTipoOperacion(UUID agenteId, TipoOperacion tipoOperacion, Pageable pageable);

    @Query("select e.tipoOperacion, count(e) from Estimacion e where e.agenteId = :agenteId group by e.tipoOperacion")
    List<Object[]> contarPorTipo(@Param("agenteId") UUID agenteId);
}
