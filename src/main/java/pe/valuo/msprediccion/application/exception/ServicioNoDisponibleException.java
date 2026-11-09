package pe.valuo.msprediccion.application.exception;

/** El servicio de ML o msinmuebles no responde (caído, timeout, error 5xx o credenciales mal configuradas). */
public class ServicioNoDisponibleException extends RuntimeException {

    public ServicioNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}
