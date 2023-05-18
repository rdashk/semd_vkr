package ru.isu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.isu.model.db.FileSemd;

import java.util.List;

@Repository
public interface FileSemdRepository extends JpaRepository<FileSemd, Long> {

    @Query("SELECT f.path FROM FileSemd f WHERE f.code = ?1")
    List<String> getFilesSemdByCode(Long code);

}
