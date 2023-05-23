package ru.isu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.isu.model.db.Semd;

import java.util.List;

@Repository
public interface SemdRepository extends JpaRepository<Semd, Long> {

    Semd getSemdByCode(Long code);

    @Query("SELECT s FROM Semd s ORDER BY s.code")
    List<Semd> getAll();
}
