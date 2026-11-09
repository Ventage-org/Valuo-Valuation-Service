package pe.valuo.msprediccion.domain.model.estimacion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

/** Estimación de precio de mercado emitida para un agente. Es inmutable: el historial no se edita. */
@Entity
@Table(name = "estimacion")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Estimacion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "agente_id", nullable = false, updatable = false)
    private UUID agenteId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_operacion", nullable = false, length = 10)
    private TipoOperacion tipoOperacion;

    @Column(name = "inmueble_id")
    private UUID inmuebleId;

    @Column(length = 150)
    private String titulo;

    @Column(length = 255)
    private String direccion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_inmueble", nullable = false, length = 20)
    private TipoInmueble tipoInmueble;

    @Column(name = "distrito_id", nullable = false)
    private Short distritoId;

    @Column(name = "distrito_nombre", nullable = false, length = 80)
    private String distritoNombre;

    @Column(name = "superficie_total_m2", nullable = false)
    private BigDecimal superficieTotalM2;

    @Column(nullable = false)
    private int habitaciones;

    @Column(nullable = false)
    private BigDecimal banos;

    @Column(nullable = false)
    private int estacionamientos;

    private Integer piso;

    @Column(name = "antiguedad_anios", nullable = false)
    private int antiguedadAnios;

    @Column(name = "vista_exterior", nullable = false)
    private boolean vistaExterior;

    @Column(name = "precio_soles", nullable = false)
    private BigDecimal precioSoles;

    @Column(name = "precio_m2_soles")
    private BigDecimal precioM2Soles;

    @Column(name = "rango_inferior_soles")
    private BigDecimal rangoInferiorSoles;

    @Column(name = "rango_superior_soles")
    private BigDecimal rangoSuperiorSoles;

    @Column(name = "tipo_cambio", nullable = false)
    private BigDecimal tipoCambio;

    @Column(name = "mape_pct")
    private BigDecimal mapePct;

    @Column(name = "version_modelo", length = 40)
    private String versionModelo;

    @Column(name = "periodo_ipc", length = 10)
    private String periodoIpc;

    @JdbcTypeCode(SqlTypes.JSON)
    private String explicabilidad;

    @JdbcTypeCode(SqlTypes.JSON)
    private String comparables;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * @param tipoCambio soles por dólar vigente al estimar
     * @param inmuebleId inmueble creado en la cartera, o nulo si el agente no lo guardó
     */
    public static Estimacion registrar(UUID agenteId, TipoOperacion tipoOperacion, CaracteristicasInmueble datos,
                                       String distritoNombre, String titulo, String direccion,
                                       ResultadoModelo resultado, BigDecimal tipoCambio, UUID inmuebleId) {
        Estimacion estimacion = new Estimacion();
        estimacion.agenteId = agenteId;
        estimacion.tipoOperacion = tipoOperacion;
        estimacion.inmuebleId = inmuebleId;
        estimacion.titulo = titulo;
        estimacion.direccion = direccion;
        estimacion.tipoInmueble = datos.tipo();
        estimacion.distritoId = datos.distritoId();
        estimacion.distritoNombre = distritoNombre;
        estimacion.superficieTotalM2 = datos.superficieTotalM2();
        estimacion.habitaciones = datos.habitaciones();
        estimacion.banos = datos.banos();
        estimacion.estacionamientos = datos.estacionamientos();
        estimacion.piso = datos.piso();
        estimacion.antiguedadAnios = datos.antiguedadAnios();
        estimacion.vistaExterior = datos.vistaExterior();
        estimacion.precioSoles = resultado.precioSoles();
        estimacion.precioM2Soles = resultado.precioM2Soles();
        estimacion.rangoInferiorSoles = resultado.rangoInferiorSoles();
        estimacion.rangoSuperiorSoles = resultado.rangoSuperiorSoles();
        estimacion.tipoCambio = tipoCambio;
        estimacion.mapePct = resultado.mapePct();
        estimacion.versionModelo = resultado.versionModelo();
        estimacion.periodoIpc = resultado.periodoIpc();
        estimacion.explicabilidad = resultado.explicabilidadJson();
        estimacion.comparables = resultado.comparablesJson();
        return estimacion;
    }

    /** Convierte un monto de esta estimación a dólares con el tipo de cambio con que se emitió. */
    public BigDecimal enDolares(BigDecimal soles) {
        return soles == null ? null : soles.divide(tipoCambio, 2, RoundingMode.HALF_UP);
    }
}
