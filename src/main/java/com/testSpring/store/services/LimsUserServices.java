package com.testSpring.store.services;

import com.testSpring.store.entities.LimsUser;
import com.testSpring.store.repositories.LimsUserRepository;
import com.testSpring.store.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class LimsUserServices {

    @Autowired
    private LimsUserRepository limsUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<LimsUser> getAllLimsUsers(){
        return (List<LimsUser>) this.limsUserRepository.findAll();
    }

    public Page<LimsUser> getAllLimsUsers(Pageable pageable) {
        return limsUserRepository.findAll(pageable);
    }

    public Optional<LimsUser> getLimsUserByEmail(String email) {
        return limsUserRepository.findByEmail(email);
    }

    public Optional<LimsUser> getLimsUser(int id){
        return this.limsUserRepository.findById(id);
    }

    public Optional<LimsUser> getLimsUserByName (String name){
        return Optional.ofNullable(this.limsUserRepository.findLimsUserByName(name));
    }

    public void updateUser (LimsUser user, int id){
        LimsUser current = limsUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el usuario " + id));
        if (user.getName() != null) current.setName(user.getName());
        // Email and activation are identity/security fields and are managed separately.
        if (user.getDepartment() != null) current.setDepartment(user.getDepartment());
        if (user.getLocation() != null) current.setLocation(user.getLocation());
        if (user.getRole() != null) current.setRole(user.getRole());
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            current.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        this.limsUserRepository.save(current);
    }

    public void deactivateUser(int id){

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
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setActive(true);
        this.limsUserRepository.save(user);
    }

    public boolean validateLoginCredentials(String email,String password)
    {
        if (email == null || email.isBlank() || password == null) return false;
        LimsUser user = limsUserRepository.findLimsUserByEmail(email);
        return user != null && user.getPassword() != null
                && passwordEncoder.matches(password, user.getPassword());
    }

}
