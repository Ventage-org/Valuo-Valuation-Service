package pe.valuo.msprediccion.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import pe.valuo.msprediccion.domain.model.Pagina;
import pe.valuo.msprediccion.domain.model.estimacion.Estimacion;
import pe.valuo.msprediccion.domain.model.estimacion.TipoOperacion;
import pe.valuo.msprediccion.domain.repository.EstimacionRepository;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
class EstimacionRepositoryAdapter implements EstimacionRepository {

    private static final Sort MAS_RECIENTES_PRIMERO = Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by("id"));

    private final SpringDataEstimacionRepository jpa;

    @Override
    public Estimacion guardar(Estimacion estimacion) {
        return jpa.save(estimacion);
    }

    @Override
    public Optional<Estimacion> buscarPorIdYAgente(UUID id, UUID agenteId) {
        return jpa.findByIdAndAgenteId(id, agenteId);
    }

    @Override
    public Pagina<Estimacion> buscar(UUID agenteId, TipoOperacion tipo, int pagina, int tamanio) {
        PageRequest pedido = PageRequest.of(pagina, tamanio, MAS_RECIENTES_PRIMERO);
        Page<Estimacion> resultado = tipo == null
                ? jpa.findByAgenteId(agenteId, pedido)
                : jpa.findByAgenteIdAndTipoOperacion(agenteId, tipo, pedido);
        return new Pagina<>(resultado.getTotalElements(), resultado.getTotalPages(), resultado.getNumber(),
                resultado.getContent());
    }

    @Override
    public Pagina<Estimacion> buscar(UUID agenteId, UUID inmuebleId, TipoOperacion tipo, int pagina, int tamanio) {
        PageRequest pedido = PageRequest.of(pagina, tamanio, MAS_RECIENTES_PRIMERO);
        Page<Estimacion> resultado = tipo == null
                ? jpa.findByAgenteIdAndInmuebleId(agenteId, inmuebleId, pedido)
                : jpa.findByAgenteIdAndInmuebleIdAndTipoOperacion(agenteId, inmuebleId, tipo, pedido);
        return new Pagina<>(resultado.getTotalElements(), resultado.getTotalPages(), resultado.getNumber(),
                resultado.getContent());
    }

    @Override
    public Optional<Estimacion> buscarUltimaPorInmueble(UUID agenteId, UUID inmuebleId) {
        return jpa.findFirstByAgenteIdAndInmuebleIdOrderByCreatedAtDesc(agenteId, inmuebleId);
    }

    @Override
    public Map<TipoOperacion, Long> contarPorTipo(UUID agenteId) {
        Map<TipoOperacion, Long> porTipo = new EnumMap<>(TipoOperacion.class);
        for (Object[] fila : jpa.contarPorTipo(agenteId)) {
            porTipo.put((TipoOperacion) fila[0], (Long) fila[1]);
        }
        return porTipo;
    }
}
