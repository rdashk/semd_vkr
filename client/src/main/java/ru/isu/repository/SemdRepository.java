package ru.isu.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import ru.isu.model.db.SemdPackage;

import java.util.List;

@Repository
public interface SemdRepository extends MongoRepository<SemdPackage, String> {

    @Query(value = "{'_id':'?0'}", fields = "{'name': 1, 'files._id': 1}")
    SemdPackage getFileNamesByCode(String code);

    @Query("{'_id': 1, 'name': 1, 'date': 1}")
    List<SemdPackage> getAllSemds();
}
