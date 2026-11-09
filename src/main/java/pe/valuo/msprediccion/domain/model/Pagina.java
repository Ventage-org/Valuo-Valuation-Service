package pe.valuo.msprediccion.domain.model;

import java.util.List;
import java.util.function.Function;

/** Página de resultados independiente de Spring Data. Los nombres coinciden con el contrato REST. */
public record Pagina<T>(long totalElementos, int totalPaginas, int paginaActual, List<T> contenido) {

    public <R> Pagina<R> map(Function<T, R> funcion) {
        return new Pagina<>(totalElementos, totalPaginas, paginaActual, contenido.stream().map(funcion).toList());
    }
}
