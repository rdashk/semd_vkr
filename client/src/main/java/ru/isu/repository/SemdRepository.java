package ru.isu.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import ru.isu.model.db.Semd;

@Repository
public interface SemdRepository extends MongoRepository<Semd, String> {

    @Query("{id:'?0'}")
    Semd findSemdByCode(String code);

    //@Query("SELECT s FROM Semd s ORDER BY s.code")
    //List<Semd> getAll();
}
