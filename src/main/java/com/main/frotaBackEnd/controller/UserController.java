package com.main.frotaBackEnd.controller;

import com.main.frotaBackEnd.model.UserRequestDTO;
import com.main.frotaBackEnd.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/autenticar")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/logar")
    public String logar(@RequestBody UserRequestDTO user) {
        return userService.logar(user);
    }
}