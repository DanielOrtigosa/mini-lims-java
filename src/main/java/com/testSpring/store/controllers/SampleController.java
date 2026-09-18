package com.testSpring.store.controllers;

import com.testSpring.store.entities.Sample;
import com.testSpring.store.services.SampleServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class SampleController {

    @Autowired
    private SampleServices services;

    @PostMapping("/addSample")
    public String addSample(@ModelAttribute Sample sample) {
        this.services.addSample(sample);
        return "redirect:/admin/services";
    }
    @GetMapping("/updatingSample/{id}")
    public String updateSample(@ModelAttribute Sample sample, @PathVariable("id") int id) {
        this.services.updateSample(sample, id);
        return "redirect:/admin/services";
    }
    @GetMapping("/deleteSample/{id}")
    public String deleteSample(@PathVariable("id") int id) {
        this.services.deleteSample(id);
        return "redirect:/admin/services";
    }
    @GetMapping("/activateSample/{id}")
    public String activateSample(@PathVariable("id") int id) {
        this.services.activateSample(id);
        return "redirect:/admin/services";
    }
}
