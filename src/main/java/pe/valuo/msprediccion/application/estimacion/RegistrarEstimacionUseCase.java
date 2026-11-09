package pe.valuo.msprediccion.application.estimacion;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.valuo.msprediccion.application.estimacion.result.EstimacionResult;
import pe.valuo.msprediccion.application.port.CarteraInmuebles;
import pe.valuo.msprediccion.application.port.MotorEstimacion;
import pe.valuo.msprediccion.application.port.ProveedorTipoCambio;
import pe.valuo.msprediccion.domain.exception.DistritoNoEncontradoException;
import pe.valuo.msprediccion.domain.exception.ValorInvalidoException;
import pe.valuo.msprediccion.domain.model.distrito.Distrito;
import pe.valuo.msprediccion.domain.model.estimacion.Estimacion;
import pe.valuo.msprediccion.domain.model.estimacion.ResultadoModelo;
import pe.valuo.msprediccion.domain.model.estimacion.TipoInmueble;
import pe.valuo.msprediccion.domain.repository.DistritoRepository;
import pe.valuo.msprediccion.domain.repository.EstimacionRepository;

import java.util.UUID;

/**
 * Pide la estimación al modelo, guarda el inmueble en la cartera si el agente lo pidió y registra la estimación
 * en su historial. Si el modelo o la cartera fallan no se guarda nada aquí.
 */
@Service
@RequiredArgsConstructor
public class RegistrarEstimacionUseCase {

    private final DistritoRepository distritoRepository;
    private final EstimacionRepository estimacionRepository;
    private final MotorEstimacion motorEstimacion;
    private final CarteraInmuebles carteraInmuebles;
    private final ProveedorTipoCambio proveedorTipoCambio;

    public EstimacionResult ejecutar(RegistrarEstimacionCommand command) {
        // El modelo se entrenó solo con departamentos; una casa no tiene respaldo (Memoria, sección 2.1.5).
        if (command.caracteristicas().tipo() != TipoInmueble.DEPARTAMENTO) {
            throw new ValorInvalidoException("La estimación solo está disponible para departamentos");
        }
        Short distritoId = command.caracteristicas().distritoId();
        Distrito distrito = distritoRepository.buscarActivoPorId(distritoId)
                .orElseThrow(() -> new DistritoNoEncontradoException(distritoId));

        ResultadoModelo resultado = motorEstimacion.estimar(command.tipoOperacion(), command.caracteristicas(),
                distrito.getNombreModelo());

        DatosCartera cartera = command.datosCartera();
        UUID inmuebleId = command.guardarEnCartera()
                ? carteraInmuebles.registrar(command.tokenAgente(), command.caracteristicas(), cartera)
                : null;

        Estimacion estimacion = Estimacion.registrar(command.agenteId(), command.tipoOperacion(),
                command.caracteristicas(), distrito.getNombre(), cartera.titulo(), cartera.direccion(), resultado,
                proveedorTipoCambio.penPorUsd(), inmuebleId);
        return EstimacionResult.de(estimacionRepository.guardar(estimacion));
    }
}
