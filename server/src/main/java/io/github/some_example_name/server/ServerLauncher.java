package io.github.some_example_name.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Launches the server application. */
@SpringBootApplication
public class ServerLauncher {
    public static void main(String[] args) {
        SpringApplication.run(ServerLauncher.class, args);
    }
}