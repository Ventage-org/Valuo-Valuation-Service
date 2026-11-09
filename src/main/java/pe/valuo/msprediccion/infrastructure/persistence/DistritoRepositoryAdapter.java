package pe.valuo.msprediccion.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import pe.valuo.msprediccion.domain.model.distrito.Distrito;
import pe.valuo.msprediccion.domain.repository.DistritoRepository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
class DistritoRepositoryAdapter implements DistritoRepository {

    private final SpringDataDistritoRepository jpa;

    @Override
    public Optional<Distrito> buscarActivoPorId(Short id) {
        return jpa.findByIdAndActivoTrue(id);
    }
}
