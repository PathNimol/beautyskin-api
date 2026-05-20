package com.acleda.bsonlineshop.security.oauth2;

import org.springframework.security.oauth2.core.user.OAuth2User;
import java.util.Map;

public class OAuth2UserInfoFactory {

    public static String getEmail(String provider, OAuth2User oAuth2User) {
        Map<String, Object> attrs = oAuth2User.getAttributes();
        if ("google".equals(provider)) {
            return (String) attrs.get("email");
        }
        throw new IllegalArgumentException("Unknown provider: " + provider);
    }

    public static String getFirstName(String provider, OAuth2User oAuth2User) {
        Map<String, Object> attrs = oAuth2User.getAttributes();
        if ("google".equals(provider)) {
            return (String) attrs.getOrDefault("given_name", "User");
        }
        return "User";
    }

    public static String getLastName(String provider, OAuth2User oAuth2User) {
        Map<String, Object> attrs = oAuth2User.getAttributes();
        if ("google".equals(provider)) {
            return (String) attrs.getOrDefault("family_name", "");
        }
        return "";
    }

    public static String getAvatar(String provider, OAuth2User oAuth2User) {
        Map<String, Object> attrs = oAuth2User.getAttributes();
        if ("google".equals(provider)) {
            return (String) attrs.get("picture");
        }
        return null;
    }
}