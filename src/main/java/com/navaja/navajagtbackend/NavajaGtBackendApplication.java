package com.navaja.navajagtbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.navaja.navajagtbackend.repositories")
public class NavajaGtBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(NavajaGtBackendApplication.class, args);
    }

}

