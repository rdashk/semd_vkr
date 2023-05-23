package ru.isu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.isu.model.db.Semd;

@Repository
public interface SemdRepository extends JpaRepository<Semd, Long> {

    Semd getSemdByCode(Long code);
}
