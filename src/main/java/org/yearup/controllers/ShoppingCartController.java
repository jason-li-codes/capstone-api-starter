package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.ProductDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;
import org.yearup.models.User;

import java.security.Principal;

// add the annotations to make this a REST controller
// add the annotation to make this controller the endpoint for the following url
// http://localhost:8080/cart
// add annotation to allow cross site origin requests
@RestController
@RequestMapping("cart")
@CrossOrigin
public class ShoppingCartController {

    // a shopping cart requires
    private ShoppingCartDao shoppingCartDao;
    private UserDao userDao;
    private ProductDao productDao;

    @Autowired
    // inject necessary dependencies
    public ShoppingCartController(ShoppingCartDao shoppingCartDao,  UserDao userDao, ProductDao productDao) {
        this.shoppingCartDao = shoppingCartDao;
        this.userDao = userDao;
        this.productDao = productDao;
    }

    // each method in this controller requires a Principal object as a parameter
    @GetMapping
    @PreAuthorize("isAuthenticated()") // check to ensure correct user is accessing method
    public ShoppingCart getCart(Principal principal) {
        // try to access correct ShoppingCart
        try {
            // get the currently logged-in username
            String userName = principal.getName();
            // find database user by userId
            User user = userDao.getUserByUserName(userName);
            // throws response status exception if user cannot be found and is therefore null
            if (user == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.");
            // get correct userId from user accessed by userDao
            int userId = user.getId();
            // use the shoppingCartDao to get all items in the cart and return the cart
            return shoppingCartDao.getByUserId(userId);
        } catch (ResponseStatusException e) {
                throw e;
        } catch (Exception e) { // throws server error if response exception is not thrown
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    // add a POST method to add a product to the cart - the url should be
    // https://localhost:8080/cart/products/15 (15 is the productId to be added
    @PostMapping("products/{productId}")
    @ResponseStatus(HttpStatus.CREATED) // gives correct HTTP status
    @PreAuthorize("isAuthenticated()")
    public ShoppingCart addProduct(Principal principal, @PathVariable int productId) {
        // try to access correct Product
        try {
            // get the currently logged-in username
            String userName = principal.getName();
            // find database user by userId
            User user = userDao.getUserByUserName(userName);
            // throws response status exception if user cannot be found and is therefore null
            if (user == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.");
            // get correct userId from user accessed by userDao
            int userId = user.getId();
            // return ShoppingCart after adding item correctly
            return shoppingCartDao.addItem(userId, new ShoppingCartItem(productDao.getById(productId)));
        }
        catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) { // throws server error if response exception is not thrown
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    // add a PUT method to update an existing product in the cart - the url should be
    // https://localhost:8080/cart/products/15 (15 is the productId to be updated)
    // the BODY should be a ShoppingCartItem - quantity is the only value that will be updated
    @PutMapping("products/{id}")
    @PreAuthorize("isAuthenticated()")
    public ShoppingCart updateCart(Principal principal, @PathVariable int id, @RequestBody ShoppingCartItem shoppingCartItem) {
        // try to access correct ShoppingCart using userDao and shoppingCartDao
        try {
            // get the currently logged-in username
            String userName = principal.getName();
            // find database user by userId
            User user = userDao.getUserByUserName(userName);
            // throws response status exception if user cannot be found and is therefore null
            if (user == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.");
            // get correct userId from user accessed by userDao
            int userId = user.getId();
            // return ShoppingCart after updating item correctly
            shoppingCartDao.updateItem(userId, shoppingCartItem);
            return shoppingCartDao.getByUserId(userId);
        }
        catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) { // throws server error if response exception is not thrown
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    // add a DELETE method to clear all products from the current users cart
    // https://localhost:8080/cart
    @DeleteMapping
    @PreAuthorize("hasRole('ROLE_USER')") // check to ensure user is logged in before accessing method
    public ShoppingCart deleteCart(Principal principal) {
        // try to access ShoppingCart using userDao and shoppingCartDao
        try {
            // get the currently logged-in username
            String userName = principal.getName();
            // find database user by userId
            User user = userDao.getUserByUserName(userName);
            // throws response status exception if user cannot be found and is therefore null
            if (user == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.");
            // get correct userId from user accessed by userDao
            int userId = user.getId();
            // clear ShoppingCart and returns empty one
            shoppingCartDao.deleteShoppingCart(userId);
            return new ShoppingCart();
        }
        catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) { // throws server error if response exception is not thrown
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

}
