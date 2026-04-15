package com.navaja.arqhexagonal.application.port.out;

import com.navaja.arqhexagonal.domain.model.User;

public interface UserRepositoryPort {
    User save (User user);
}
