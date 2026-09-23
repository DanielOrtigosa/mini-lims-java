package com.testSpring.store.controllers;

import com.testSpring.store.entities.Status;
import com.testSpring.store.services.StatusServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class StatusController {

    @Autowired
    private StatusServices services;

    @PostMapping("/addStatus")
    public String addStatus(@ModelAttribute Status status) {
        this.services.addStatus(status);
        return "redirect:/admin/services";
    }
    @GetMapping("/updatingStatus/{id}")
    public String updateStatus(@ModelAttribute Status status, @PathVariable("id") int id) {
        this.services.updateStatus(status, id);
        return "redirect:/admin/services";
    }
    @DeleteMapping("/deleteStatus/{id}")
    public String deleteStatus(@PathVariable("id") int id) {
        this.services.deleteStatus(id);
        return "redirect:/admin/services";
    }
}
