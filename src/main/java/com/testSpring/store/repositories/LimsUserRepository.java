package com.testSpring.store.repositories;

import com.testSpring.store.entities.LimsUser;
import org.springframework.data.repository.CrudRepository;

public interface LimsUserRepository extends CrudRepository<LimsUser,Integer> {
    LimsUser findLimsUserByName(String name);
    LimsUser findLimsUserByEmail(String email);
}
