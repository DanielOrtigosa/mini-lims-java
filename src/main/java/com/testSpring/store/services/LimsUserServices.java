package com.testSpring.store.services;

import com.testSpring.store.entities.LimsUser;
import com.testSpring.store.repositories.LimsUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

@Service
public class LimsUserServices {

    @Autowired
    private LimsUserRepository limsUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<LimsUser> getAllLimsUsers(){
        return (List<LimsUser>) this.limsUserRepository.findAll();
    }

    public Optional<LimsUser> getLimsUser(int id){
        Optional<LimsUser> optional = this.limsUserRepository.findById(id);

        if (optional.isEmpty()){
            return null;
        }
        return optional;
    }

    public Optional<LimsUser> getLimsUserByName (String name){
        return Optional.ofNullable(this.limsUserRepository.findLimsUserByName(name));
    }

    public void updateUser (LimsUser user, int id){
        user.setID(id);
        this.limsUserRepository.save(user);
    }

    public void deleteUser(int id){

        Optional<LimsUser> user = this.limsUserRepository.findById(id);
        if (user.isPresent()) {
            user.get().setActive(false);
            this.limsUserRepository.save(user.get());
        }
    }

    public void activateUser(int id){
        Optional<LimsUser> user = this.limsUserRepository.findById(id);
        if (user.isPresent()){
            user.get().setActive(true);
            this.limsUserRepository.save(user.get());
        }
    }

    public void addUser(LimsUser user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        this.limsUserRepository.save(user);
    }

    public boolean validateLoginCredentials(String email,String password)
    {
        List<LimsUser> users = (List<LimsUser>) this.limsUserRepository.findAll();
        for(LimsUser u:users)
        {
            if(u!=null && passwordEncoder.matches(password, u.getPassword()) && u.getEmail().equals(email))
            {
                return true;
            }
        }
        return false;
    }

}
