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

// add the annotations to make this a REST controller
// add the annotation to make this controller the endpoint for the following url
// http://localhost:8080/profile
// add annotation to allow cross site origin requests
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