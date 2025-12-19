package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.ProfileDao;
import org.yearup.data.UserDao;
import org.yearup.models.Profile;
import org.yearup.models.User;

import java.security.Principal;

/**
 * REST controller responsible for handling user profile–related API requests.
 * This controller provides endpoints for authenticated users to retrieve and update
 * their own profile information. Access is restricted to the currently logged-in
 * user, ensuring that profile data cannot be viewed or modified by other users.
 * The controller relies on UserDao to identify the authenticated user and ProfileDao
 * to perform profile data retrieval and updates. Cross-origin requests are enabled
 * to support frontend applications.
 */
@RestController
@RequestMapping("profile")
@CrossOrigin
public class ProfileController {
    // this controller requires the following dependencies
    private ProfileDao profileDao;
    private UserDao userDao;

    @Autowired
    // constructor creates the necessary dependencies
    public ProfileController(ProfileDao profileDao, UserDao userDao) {
        this.profileDao = profileDao;
        this.userDao = userDao;
    }

    /**
     * Retrieves the profile for the currently authenticated user.
     * This method uses the authenticated user’s security principal to determine the
     * associated user account and returns the profile linked to that user. If an error
     * occurs while retrieving the profile, an appropriate HTTP error response is returned.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()") // check to ensure correct user is accessing method
    public Profile getProfileById(Principal principal) {
        // try to access Profile using userDao and profileDao
        try {
            // get the currently logged-in username
            String userName = principal.getName();
            // find database user by userId
            User user = userDao.getUserByUserName(userName);
            int userId = user.getId();
            // use the profileDao to get the right profile
            return profileDao.getProfileByUserId(userId);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) { // throws server error if response exception is not thrown
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    /**
     * Updates the profile for the currently authenticated user.
     * This method allows an authenticated user to modify their own profile information.
     * The update is applied only to the profile associated with the current user. If an
     * error occurs during the update process, an appropriate HTTP error response is
     * returned.
     */
    @PutMapping
    @PreAuthorize("isAuthenticated()") // check to ensure correct user is accessing method
    public Profile updateProfile(Principal principal, @RequestBody Profile profile) {
        // try to access Profile using userDao and profileDao
        try {
            // get the currently logged-in username
            String userName = principal.getName();
            // find database user by userId
            User user = userDao.getUserByUserName(userName);
            int userId = user.getId();
            // use profileDao to return correct Profile
            return profileDao.update(userId, profile);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) { // throws server error if response exception is not thrown
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }
}