package pe.valuo.msprediccion.domain.exception;

/** Se lanza también cuando la estimación existe pero es de otro agente: no se revela su existencia. */
public class EstimacionNoEncontradaException extends DomainException {

    public EstimacionNoEncontradaException() {
        super("Estimación no encontrada");
    }
}
