package pe.valuo.msprediccion.application.exception;

import lombok.Getter;

/** Un servicio externo rechazó la solicitud (4xx): se reenvía al cliente su código, su detalle y sus errores. */
@Getter
public class ServicioExternoException extends RuntimeException {

    private final int status;
    private final transient Object errores;

    public ServicioExternoException(int status, String detalle, Object errores) {
        super(detalle);
        this.status = status;
        this.errores = errores;
    }
}
