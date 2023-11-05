package com.example.userservice.constants;

public class UserConstants {
    public static final String ERROR_MESSAGE = "errorMessage";
    public static final String STATUS = "status";
    public static final String BEARER = "BEARER";
    public static final String LOGOUT_DESCRIPTION = "Logs out a user." +
            " Provide the bearer token and the username of the user";
    public static final String LOGIN_DESCRIPTION = "Logs in a user. Requires username and password";
    public static final String REGISTER_DESCRIPTION = "Registers a user as customer." +
            " Returns the profile of the registered customer.";
    public static final String REGISTER_ADMIN_DESCRIPTION = "Registers a user as admin. " +
            "Allowed access to admin." +
            " Returns the profile of the registered admin.";
    public static final String UPDATE_DESCRIPTION = "Updates a user." +
            "A user can update its own profile only. " +
            "An admin can update any profile.";
    public static final String VIEW_USER_DESCRIPTION = "View a user profile." +
            "A user can view only his profile." +
            "Admin can view any profile." +
            "Returns the user profile if enabled.";
    public static final String VIEW_ALL_DESCRIPTION = "Returns a list of all the registered and enabled users." +
            "Allowed access to only admin." +
            "Pass a parameter 'disabled=1' to view disabled users too.";
    public static final String ENABLE_DESCRIPTION = "Enables a user. Allowed to only admin.";

    public static final String DISABLE_DESCRIPTION = "Disables a users. Allowed to only admin.";

    public static final String DELETE_DESCRIPTION = "Deletes a user. Allowed to only admin";

    public static final String BAD_REQUEST_DESCRIPTION = "Bearer token not present or unauthorized/wrong/expired token";

    public static final String SERVER_ERROR_DESCRIPTION = "Internal server error regarding queries or other error";

    private UserConstants() {
    }


}
