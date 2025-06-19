package client.managers;

import common.dto.User;

/**
 * A simple client-side manager to hold the current user's credentials.
 * This class ensures that the user's state (logged in or not) is maintained
 * throughout the client session.
 */
public class UserManager {
    private User currentUser;

    /**
     * @return The current logged-in user, or null if no one is logged in.
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Sets the current user, effectively logging them in.
     * @param user The user object containing username and password.
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    /**
     * @return true if a user is currently logged in, false otherwise.
     */
    public boolean isUserLoggedIn() {
        return currentUser != null;
    }

    /**
     * Clears the current user's data, effectively logging them out.
     */
    public void logout() {
        this.currentUser = null;
    }
}