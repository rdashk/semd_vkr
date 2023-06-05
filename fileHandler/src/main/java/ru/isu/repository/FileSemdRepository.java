package ru.isu.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import ru.isu.model.db.FileSemd;

import java.util.List;

@Repository
public interface FileSemdRepository extends MongoRepository<FileSemd, String> {

    @Query(value = "{code:'?0'}", fields = "{_id: 1}")
    List<String> findFilesByCode(String code);

    @Query(value = "{code:'?0'}")
    List<FileSemd> findFiles(String code);

    @Query(value = "{_id: '?0'}")
    FileSemd findFileSemdById(String code);

    boolean existsFileSemdByCode(String code);
}
