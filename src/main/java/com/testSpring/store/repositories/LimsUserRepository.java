package com.testSpring.store.repositories;

import com.testSpring.store.entities.LimsUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LimsUserRepository extends JpaRepository<LimsUser,Integer> {
    LimsUser findLimsUserByName(String name);
    LimsUser findLimsUserByEmail(String email);
    Optional<LimsUser> findByEmail(String email);
}
