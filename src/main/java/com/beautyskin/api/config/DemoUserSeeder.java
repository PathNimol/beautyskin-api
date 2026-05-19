package com.beautyskin.api.config;

import com.beautyskin.api.model.entity.User;
import com.beautyskin.api.model.enums.UserRole;
import com.beautyskin.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DemoUserSeeder implements ApplicationRunner {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    if (userRepository.count() > 0) {
      return;
    }

    log.info("Seeding demo users (matches beautyskin-ui mock accounts)");

    seed(
        "admin@beautyskin.com",
        "admin123",
        "Alex",
        "Morgan",
        UserRole.ADMIN,
        null,
        "https://img.rocket.new/generatedImages/rocket_gen_img_1b99e36f9-1763295192405.png",
        "+1 555-0001");
    seed(
        "owner@beautyskin.com",
        "owner123",
        "Sarah",
        "Chen",
        UserRole.OWNER,
        "shop-001",
        "https://img.rocket.new/generatedImages/rocket_gen_img_1024326cd-1773148666772.png",
        "+1 555-0002");
    seed(
        "owner2@beautyskin.com",
        "owner123",
        "Ji-Yeon",
        "Park",
        UserRole.OWNER,
        "shop-002",
        "https://img.rocket.new/generatedImages/rocket_gen_img_1b83dec3d-1772544715826.png",
        "+1 555-0003");
    seed(
        "staff@beautyskin.com",
        "staff123",
        "Mia",
        "Johnson",
        UserRole.STAFF,
        "shop-001",
        "https://img.rocket.new/generatedImages/rocket_gen_img_107bcec45-1773085527984.png",
        "+1 555-0004");
    seed(
        "buyer@beautyskin.com",
        "buyer123",
        "Emma",
        "Rodriguez",
        UserRole.CUSTOMER,
        null,
        "https://img.rocket.new/generatedImages/rocket_gen_img_16b7f3773-1772140653588.png",
        "+855 12 345 678");
  }

  private void seed(
      String email,
      String rawPassword,
      String firstName,
      String lastName,
      UserRole role,
      String shopId,
      String avatarUrl,
      String phone) {

    userRepository.save(
        User.builder()
            .email(email)
            .passwordHash(passwordEncoder.encode(rawPassword))
            .firstName(firstName)
            .lastName(lastName)
            .role(role)
            .shopId(shopId)
            .avatarUrl(avatarUrl)
            .phone(phone)
            .enabled(true)
            .build());
  }
}
