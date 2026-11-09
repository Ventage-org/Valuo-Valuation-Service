package pe.valuo.msprediccion.application.estimacion;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.valuo.msprediccion.application.estimacion.result.KpisResult;
import pe.valuo.msprediccion.domain.model.estimacion.TipoOperacion;
import pe.valuo.msprediccion.domain.repository.EstimacionRepository;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConsultarKpisUseCase {

    private final EstimacionRepository estimacionRepository;

    @Transactional(readOnly = true)
    public KpisResult ejecutar(UUID agenteId) {
        Map<TipoOperacion, Long> porTipo = estimacionRepository.contarPorTipo(agenteId);
        long ventas = porTipo.getOrDefault(TipoOperacion.VENTA, 0L);
        long alquileres = porTipo.getOrDefault(TipoOperacion.ALQUILER, 0L);
        return new KpisResult(ventas + alquileres, ventas, alquileres);
    }
}
