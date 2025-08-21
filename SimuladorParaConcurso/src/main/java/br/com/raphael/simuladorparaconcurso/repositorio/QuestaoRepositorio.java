package br.com.raphael.simuladorparaconcurso.repositorio;

import br.com.raphael.simuladorparaconcurso.dominio.Questao;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface QuestaoRepositorio extends JpaRepository<Questao, Long> {

    @Query(value = """
        SELECT * FROM questoes
         WHERE area_id = :areaId AND ativo = true
         ORDER BY random()
         LIMIT :n
    """, nativeQuery = true)
    List<Questao> buscarAleatoriasPorArea(@Param("areaId") Long areaId, @Param("n") int n);

    int countByAreaConhecimentoIdAndAtivoTrue(Long areaId);
}
