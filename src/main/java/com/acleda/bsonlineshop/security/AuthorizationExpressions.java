package com.acleda.bsonlineshop.security;

import com.acleda.bsonlineshop.enums.UserRole;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Bean-based policy checks referenced from {@link org.springframework.security.access.prepost.PreAuthorize}.
 */
@Component("authz")
public final class AuthorizationExpressions {

    /** Platform admin bypasses merchant scoping checks. */
    public boolean admin() {
        return currentRoleOrNull() == UserRole.ADMIN;
    }

    /** Owner or shop staff tied to {@link UserPrincipal#getShopId()}. */
    public boolean merchant() {
        UserRole r = currentRoleOrNull();
        return r == UserRole.OWNER || r == UserRole.STAFF;
    }

    public boolean storefrontUser() {
        UserRole r = currentRoleOrNull();
        return r == UserRole.CUSTOMER || r == UserRole.ADMIN;
    }

    /** Admin or storefront customer flows (cart, checkout, place order). */
    public boolean storeOrderActor() {
        return storefrontUser();
    }

    /**
     * Admin can access any shop. Merchants access only {@code User.shopId}.
     */
    public boolean merchantInShop(UUID shopId) {
        if (shopId == null) {
            throw new AccessDeniedException("Missing shop scope");
        }
        UserPrincipal p = requirePrincipal();
        if (p.getRole() == UserRole.ADMIN) {
            return true;
        }
        if (!(p.getRole() == UserRole.OWNER || p.getRole() == UserRole.STAFF)) {
            return false;
        }
        return p.getShopId() != null && p.getShopId().equals(shopId);
    }

    /** Admin or any merchant authenticated user (scoped actions still validate shop separately). */
    public boolean adminOrMerchant() {
        UserRole r = currentRoleOrNull();
        return r == UserRole.ADMIN || r == UserRole.OWNER || r == UserRole.STAFF;
    }

    private static UserPrincipal requirePrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Not authenticated");
        }
        if (!(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new AccessDeniedException("Invalid principal");
        }
        return principal;
    }

    private static UserRole currentRoleOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        if (!(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            return null;
        }
        return principal.getRole();
    }
}
