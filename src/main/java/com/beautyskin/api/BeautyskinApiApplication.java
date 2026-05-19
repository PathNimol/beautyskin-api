package com.beautyskin.api;

import com.beautyskin.api.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class BeautyskinApiApplication {

  public static void main(String[] args) {
    SpringApplication.run(BeautyskinApiApplication.class, args);
  }
}
