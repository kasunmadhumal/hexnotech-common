package com.hexnotech.commons.constants;

import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

/**
 * Represents an authenticated user in the Hexnotech platform.
 * <p>
 * Implementations should populate this from the security context
 * (e.g., JWT claims, Spring Security Authentication).
 * </p>
 */
@Getter
@Builder
public class User {

    /** Unique identifier of the user (e.g., username or subject claim). */
    private String id;

    /** Display name of the user. */
    private String name;

    /** Email address of the user. */
    private String email;

    /** List of privilege strings assigned to this user (e.g., "module.action.resource"). */
    @Builder.Default
    private List<String> privileges = Collections.emptyList();

    /** List of role names assigned to this user. */
    @Builder.Default
    private List<String> roles = Collections.emptyList();

    /**
     * Checks whether the user holds a specific privilege.
     *
     * @param privilege the privilege string to check
     * @return {@code true} if the user has the privilege
     */
    public boolean hasPrivilege(String privilege) {
        return privileges != null && privileges.contains(privilege);
    }

    /**
     * Checks whether the user holds a specific role.
     *
     * @param role the role name to check
     * @return {@code true} if the user has the role
     */
    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }
}
