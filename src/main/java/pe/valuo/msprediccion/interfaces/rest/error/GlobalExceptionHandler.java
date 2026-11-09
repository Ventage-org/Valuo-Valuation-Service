package pe.valuo.msprediccion.interfaces.rest.error;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import pe.valuo.msprediccion.application.exception.ServicioExternoException;
import pe.valuo.msprediccion.application.exception.ServicioNoDisponibleException;
import pe.valuo.msprediccion.domain.exception.DistritoNoEncontradoException;
import pe.valuo.msprediccion.domain.exception.EstimacionNoEncontradaException;
import pe.valuo.msprediccion.domain.exception.ValorInvalidoException;

import java.util.LinkedHashMap;
import java.util.Map;

/** Traduce excepciones de dominio y de aplicación a respuestas HTTP (RFC 9457 ProblemDetail). */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({ValorInvalidoException.class, DistritoNoEncontradoException.class})
    ProblemDetail solicitudInvalida(RuntimeException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(EstimacionNoEncontradaException.class)
    ProblemDetail estimacionNoEncontrada(EstimacionNoEncontradaException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(ServicioNoDisponibleException.class)
    ProblemDetail servicioNoDisponible(ServicioNoDisponibleException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
    }

    /** Reenvía el rechazo de otro servicio (p. ej. 409 si ya hay un entrenamiento en curso). */
    @ExceptionHandler(ServicioExternoException.class)
    ProblemDetail rechazoDeServicioExterno(ServicioExternoException e) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(e.getStatus()), e.getMessage());
        if (e.getErrores() != null) {
            problema.setProperty("errores", e.getErrores());
        }
        return problema;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errores.putIfAbsent(error.getField(), error.getDefaultMessage()));

        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Datos de entrada inválidos");
        problema.setProperty("errores", errores);
        return ResponseEntity.badRequest().body(problema);
    }
}
