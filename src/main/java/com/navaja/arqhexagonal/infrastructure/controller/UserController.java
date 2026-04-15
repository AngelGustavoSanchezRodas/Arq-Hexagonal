package com.navaja.arqhexagonal.infrastructure.controller;

import com.navaja.arqhexagonal.application.port.in.CreateUserUseCase;
import com.navaja.arqhexagonal.domain.model.User;
import com.navaja.arqhexagonal.infrastructure.controller.dto.UserRequest;
import com.navaja.arqhexagonal.infrastructure.controller.dto.UserResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private final CreateUserUseCase createUserUseCase;

    public UserController(CreateUserUseCase createUseruseCase) {
        this.createUserUseCase = createUseruseCase;
    }

    @PostMapping
    public void createUser(@RequestBody UserRequest userRequest) {
        final User user = new User(null, userRequest.firstName(), userRequest.lastName());
        final User userCreated = createUserUseCase.createUser(user);
        return new UserResponse(userCreated.id(), userCreated.firstName(), userCreated.lastName());
    }

}
