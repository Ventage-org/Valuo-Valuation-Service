package pe.valuo.msprediccion.domain.repository;

import pe.valuo.msprediccion.domain.model.Pagina;
import pe.valuo.msprediccion.domain.model.estimacion.Estimacion;
import pe.valuo.msprediccion.domain.model.estimacion.TipoOperacion;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface EstimacionRepository {

    Estimacion guardar(Estimacion estimacion);

    /** Filtra por agente: una estimación ajena se comporta como inexistente. */
    Optional<Estimacion> buscarPorIdYAgente(UUID id, UUID agenteId);

    /** Más recientes primero. Con {@code tipo} nulo devuelve ventas y alquileres. */
    Pagina<Estimacion> buscar(UUID agenteId, TipoOperacion tipo, int pagina, int tamanio);

    /** Tipos sin estimaciones no aparecen en el mapa. */
    Map<TipoOperacion, Long> contarPorTipo(UUID agenteId);
}
