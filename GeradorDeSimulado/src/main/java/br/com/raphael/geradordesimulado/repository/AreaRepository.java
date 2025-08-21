package br.com.raphael.geradordesimulado.repository;

import br.com.raphael.geradordesimulado.domain.Area;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AreaRepository extends JpaRepository<Area, Long> {
    default List<Area> findAllOrderByName() {
        return findAll(Sort.by("name").ascending());
    }

    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
