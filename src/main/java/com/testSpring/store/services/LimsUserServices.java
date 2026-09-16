package com.testSpring.store.services;

import com.testSpring.store.entities.LimsUser;
import com.testSpring.store.repositories.LimsUserRepository;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LimsUserServices {

    @Autowired
    private LimsUserRepository limsUserRepository;

    public List<LimsUser> getAllLimsUsers(){
        List<LimsUser> users = (List<LimsUser>) this.limsUserRepository.findAll();
        return users;
    }

    public List<LimsUser> getLimsUsers(int id){
        List<LimsUser> optional = (List<LimsUser>) this.limsUserRepository.findAll();
        if (optional.isEmpty()){
            return null;
        }
        return optional;
    }


}
