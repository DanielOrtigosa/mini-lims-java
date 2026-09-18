package com.testSpring.store.controllers;

import com.testSpring.store.entities.LimsUser;
import com.testSpring.store.services.LimsUserServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Optional;

@Controller
public class LimsUserController {

    @Autowired
    private LimsUserServices services;

    @PostMapping("/addUser")
    public String addUser(@ModelAttribute LimsUser user){
        System.out.println(user);
        this.services.addUser(user);
        return "redirect:/admin/services";
    }

    @GetMapping("/updatingUser/{id}")
    public String updateUser(@ModelAttribute LimsUser user, @PathVariable("id") int id){
        this.services.updateUser(user,id);
        return "redirect:/admin/services";
    }

    @GetMapping("/deleteUser/{id}")
    public String deleteUser(@PathVariable("id")int id){
        Optional<LimsUser> user = this.services.getLimsUser(id);
        user.get().setActive(false);

        this.services.updateUser(user.get(), id);

        return "redirect:/admin/services";
    }
}
