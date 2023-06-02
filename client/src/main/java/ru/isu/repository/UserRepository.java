package ru.isu.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import ru.isu.model.db.SystemUser;

@Repository
public interface UserRepository extends MongoRepository<SystemUser, String> {

    @Query(value = "{_id:'?0'}")
    SystemUser findSystemUserById(String chatId);
}
