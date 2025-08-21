package br.com.raphael.geradordesimulado.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "areas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Area {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "{area.name.required}")
    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @Builder.Default
    private boolean active = true;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

}
