package pe.valuo.msprediccion.application.estimacion;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.valuo.msprediccion.application.estimacion.result.EstimacionResult;
import pe.valuo.msprediccion.domain.exception.EstimacionNoEncontradaException;
import pe.valuo.msprediccion.domain.repository.EstimacionRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConsultarEstimacionUseCase {

    private final EstimacionRepository estimacionRepository;

    @Transactional(readOnly = true)
    public EstimacionResult ejecutar(UUID agenteId, UUID estimacionId) {
        return estimacionRepository.buscarPorIdYAgente(estimacionId, agenteId)
                .map(EstimacionResult::de)
                .orElseThrow(EstimacionNoEncontradaException::new);
    }
}
