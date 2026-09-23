package com.testSpring.store.controllers;

import com.testSpring.store.entities.Result;
import com.testSpring.store.services.ResultServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class ResultController {

    @Autowired
    private ResultServices services;

    @PostMapping("/addResult")
    public String addResult(@ModelAttribute Result result) {
        this.services.addResult(result);
        return "redirect:/admin/services";
    }

    @GetMapping("/updatingResult/{id}")
    public String updateResult(@ModelAttribute Result result, @PathVariable("id") int id) {
        this.services.updateResult(result, id);
        return "redirect:/admin/services";
    }

    @DeleteMapping("/deleteResult/{id}")
    public String deleteResult(@PathVariable("id") int id) {
        this.services.deleteResult(id);
        return "redirect:/admin/services";
    }
}
