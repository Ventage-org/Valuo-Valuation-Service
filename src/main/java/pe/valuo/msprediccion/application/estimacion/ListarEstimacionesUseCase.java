package pe.valuo.msprediccion.application.estimacion;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.valuo.msprediccion.application.estimacion.result.EstimacionResumenResult;
import pe.valuo.msprediccion.domain.model.Pagina;
import pe.valuo.msprediccion.domain.model.estimacion.TipoOperacion;
import pe.valuo.msprediccion.domain.repository.EstimacionRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListarEstimacionesUseCase {

    private final EstimacionRepository estimacionRepository;

    @Transactional(readOnly = true)
    public Pagina<EstimacionResumenResult> ejecutar(UUID agenteId, UUID inmuebleId, TipoOperacion tipo, int pagina, int tamanio) {
        return (inmuebleId == null
                ? estimacionRepository.buscar(agenteId, tipo, pagina, tamanio)
                : estimacionRepository.buscar(agenteId, inmuebleId, tipo, pagina, tamanio))
                .map(EstimacionResumenResult::de);
    }
}
