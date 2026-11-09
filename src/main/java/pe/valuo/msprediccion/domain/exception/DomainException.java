package pe.valuo.msprediccion.domain.exception;

/** Base de las violaciones de reglas de negocio del bounded context Estimación de Precio de Mercado. */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String mensaje) {
        super(mensaje);
    }
}
