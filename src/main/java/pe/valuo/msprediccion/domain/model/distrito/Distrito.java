package pe.valuo.msprediccion.domain.model.distrito;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Catálogo de solo lectura: copia local de los 22 distritos BCRP, sembrada por Flyway. */
@Entity
@Table(name = "distrito")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Distrito {

    @Id
    private Short id;

    @Column(nullable = false, length = 80)
    private String nombre;

    /** Nombre con el que el modelo de ML conoce al distrito (p. ej. "Jesus Maria", sin tilde). */
    @Column(name = "nombre_modelo", nullable = false, length = 80)
    private String nombreModelo;

    @Column(nullable = false)
    private boolean activo;
}
