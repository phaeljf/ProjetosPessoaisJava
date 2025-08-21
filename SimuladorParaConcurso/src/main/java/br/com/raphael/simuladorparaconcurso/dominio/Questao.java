package br.com.raphael.simuladorparaconcurso.dominio;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "questoes")
@Getter @Setter
public class Questao {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "area_id")
    private AreaConhecimento areaConhecimento;

    @Column(name = "enunciado", nullable = false, columnDefinition = "text")
    private String enunciado;

    @Column(name = "alternativa_a", nullable = false)
    private String alternativaA;

    @Column(name = "alternativa_b", nullable = false)
    private String alternativaB;

    @Column(name = "alternativa_c", nullable = false)
    private String alternativaC;

    @Column(name = "alternativa_d", nullable = false)
    private String alternativaD;

    @Column(name = "correta", nullable = false, length = 1)
    private char correta;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    // --- normalização ---

    public void setCorreta(char c) {
        this.correta = Character.toUpperCase(c);
    }

    @PrePersist @PreUpdate
    private void normalize() {
        if (enunciado != null)      enunciado   = enunciado.trim();
        if (alternativaA != null)   alternativaA = alternativaA.trim();
        if (alternativaB != null)   alternativaB = alternativaB.trim();
        if (alternativaC != null)   alternativaC = alternativaC.trim();
        if (alternativaD != null)   alternativaD = alternativaD.trim();
        this.correta = Character.toUpperCase(this.correta);
    }
}
