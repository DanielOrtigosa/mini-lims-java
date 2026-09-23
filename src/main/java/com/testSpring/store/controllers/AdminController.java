package com.testSpring.store.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AdminController {

    @GetMapping("/admin/services")
    @ResponseBody
    public String services() {
        return "Operación realizada correctamente.";
    }
}
