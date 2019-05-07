package com.user.service;

import org.springframework.security.access.annotation.Secured;

public class TestService {
    @Secured("ROLE_USER")
    public void method()
    {
        System.out.println("Method called");
    }

}


