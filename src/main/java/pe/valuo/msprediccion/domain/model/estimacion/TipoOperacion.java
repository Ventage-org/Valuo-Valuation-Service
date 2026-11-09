package pe.valuo.msprediccion.domain.model.estimacion;

import pe.valuo.msprediccion.domain.exception.ValorInvalidoException;

import java.util.Locale;

public enum TipoOperacion {
    VENTA,
    ALQUILER;

    /** Acepta "venta", "VENTA", "Alquiler"...: el panel de monitoreo usa minúsculas, como el servicio de ML. */
    public static TipoOperacion desde(String valor) {
        for (TipoOperacion tipo : values()) {
            if (tipo.name().equalsIgnoreCase(valor)) {
                return tipo;
            }
        }
        throw new ValorInvalidoException("Tipo de operación inválido: " + valor + " (use venta o alquiler)");
    }

    /** Forma en minúsculas que usan las rutas del servicio de ML. */
    public String clave() {
        return name().toLowerCase(Locale.ROOT);
    }
}
