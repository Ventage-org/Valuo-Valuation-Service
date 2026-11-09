package pe.valuo.msprediccion.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.valuo.msprediccion.domain.model.distrito.Distrito;

import java.util.Optional;

interface SpringDataDistritoRepository extends JpaRepository<Distrito, Short> {

    Optional<Distrito> findByIdAndActivoTrue(Short id);
}
