package com.example.userservice.model;

public enum UserRole {
    ADMIN,
    CUSTOMER;

    public static UserRole fromString(String roleString) {
        if (roleString != null) {
            for (UserRole role : UserRole.values()) {
                if (roleString.equalsIgnoreCase(role.name())) {
                    return role;
                }
            }
        }
        throw new IllegalArgumentException("No enum constant " + UserRole.class.getName() + "." + roleString);
    }
}
