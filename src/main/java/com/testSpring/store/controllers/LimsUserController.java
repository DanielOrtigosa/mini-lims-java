package com.testSpring.store.controllers;

import com.testSpring.store.entities.LimsUser;
import com.testSpring.store.services.LimsUserServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


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
        if (services.getLimsUser(id).isEmpty()) return "redirect:/admin/services";
        this.services.updateUser(user,id);
        return "redirect:/admin/services";
    }

    @DeleteMapping("/deleteUser/{id}")
    public String deleteUser(@PathVariable("id")int id){
        this.services.deleteUser(id);

        return "redirect:/admin/services";
    }
}
