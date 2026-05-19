package com.acleda.bsonlineshop;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableConfigurationProperties()
@EnableAsync
@OpenAPIDefinition(
        info = @Info(
                title = "Beauty Skin API",
                version = "v1",
                description = "Beauty Skin online shop — monolithic Spring Boot backend"
        ),
        servers = @Server(url = "/api")  // ← change "/" to "/api"
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class BeautySkinApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(BeautySkinApiApplication.class, args);
        System.out.println("==============| APPLICATION RUN SUCCESSFULLY |===============");
    }
}
