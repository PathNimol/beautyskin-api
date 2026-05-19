package com.acleda.bsonlineshop.security;

import com.acleda.bsonlineshop.enums.UserRole;
import com.acleda.bsonlineshop.exception.BusinessException;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static UserPrincipal currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            throw new BusinessException("Not authenticated");
        }
        return principal;
    }

    public static UUID currentUserId() {
        return currentUser().getId();
    }

    public static UserRole currentRole() {
        return currentUser().getRole();
    }

    public static UUID requireShopId(UUID requestedShopId) {
        UserPrincipal user = currentUser();
        if (user.getRole() == UserRole.ADMIN) {
            return requestedShopId;
        }
        if (user.getShopId() == null) {
            throw new BusinessException("User has no shop assigned");
        }
        if (requestedShopId != null && !requestedShopId.equals(user.getShopId())) {
            throw new BusinessException("Access denied for this shop");
        }
        return user.getShopId();
    }
}
