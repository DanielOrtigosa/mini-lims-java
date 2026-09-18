package com.testSpring.store.controllers;

import com.testSpring.store.entities.Status;
import com.testSpring.store.services.StatusServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

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
    @GetMapping("/deleteStatus/{id}")
    public String deleteStatus(@PathVariable("id") int id) {
        this.services.deleteStatus(id);
        return "redirect:/admin/services";
    }
}
