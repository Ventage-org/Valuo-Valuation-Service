package pe.valuo.msprediccion.domain.exception;

public class DistritoNoEncontradoException extends DomainException {

    public DistritoNoEncontradoException(Short distritoId) {
        super("El distrito indicado no existe: " + distritoId);
    }
}
