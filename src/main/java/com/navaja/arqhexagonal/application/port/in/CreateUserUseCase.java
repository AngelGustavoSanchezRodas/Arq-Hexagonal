package com.navaja.arqhexagonal.application.port.in;

import com.navaja.arqhexagonal.domain.model.User;

public interface CreateUserUseCase {
    User createUser(User user);
}
