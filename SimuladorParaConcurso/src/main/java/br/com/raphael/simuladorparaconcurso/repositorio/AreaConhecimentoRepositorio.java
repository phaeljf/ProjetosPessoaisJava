package br.com.raphael.simuladorparaconcurso.repositorio;

import br.com.raphael.simuladorparaconcurso.dominio.AreaConhecimento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AreaConhecimentoRepositorio extends JpaRepository<AreaConhecimento, Long> {
    List<AreaConhecimento> findByAtivoTrueOrderByNomeAsc();
}
