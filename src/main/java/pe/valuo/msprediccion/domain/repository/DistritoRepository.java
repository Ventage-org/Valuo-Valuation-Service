package pe.valuo.msprediccion.domain.repository;

import pe.valuo.msprediccion.domain.model.distrito.Distrito;

import java.util.Optional;

public interface DistritoRepository {

    Optional<Distrito> buscarActivoPorId(Short id);
}
