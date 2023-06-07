package ru.isu.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import ru.isu.model.db.SemdPackage;

import java.util.Date;

@Repository
public interface SemdRepository extends MongoRepository<SemdPackage, String> {

    @Query(value = "{'_id':'?0'}", fields = "{'date': 1}")
    Date findDateSemdByCode(String code);

    @Query(value = "{'_id':'?0'}", fields = "{'_id': 0, 'name': 1}")
    SemdPackage findSemdNameByCode(String code);

    @Query(value = "{'_id':'?0'}", fields = "{'files._id': 1}")
    SemdPackage getFileNamesByCode(String code);

    @Query(value = "{'_id':'?0'}", fields = "{'files': 1}")
    SemdPackage getFilesByCode(String code);
}
