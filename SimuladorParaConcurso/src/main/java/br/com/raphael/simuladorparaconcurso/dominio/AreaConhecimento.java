package br.com.raphael.simuladorparaconcurso.dominio;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "areas_conhecimento")
@Getter @Setter
public class AreaConhecimento {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true)
    private String nome;

    private String descricao;

    @Column(nullable=false)
    private boolean ativo = true;
}
