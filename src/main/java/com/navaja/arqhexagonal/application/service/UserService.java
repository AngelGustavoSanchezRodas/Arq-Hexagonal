package com.navaja.arqhexagonal.application.service;

import com.navaja.arqhexagonal.application.port.in.CreateUserUseCase;
import com.navaja.arqhexagonal.application.port.out.UserRepositoryPort;
import com.navaja.arqhexagonal.domain.model.User;
import org.springframework.stereotype.Service;

@Service
public class UserUseService implements CreateUserUseCase {

    private final UserRepositoryPort userRepositoryPort;

    public UserUseService(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public User createUser(User user) {
        return userRepositoryPort.save(user);
    }
}
