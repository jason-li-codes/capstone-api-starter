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

/**
 * REST controller responsible for handling shopping-cart related API requests.
 * This controller allows authenticated users to view and manage their shopping cart.
 * Supported operations include retrieving the current cart, adding products, updating
 * product quantities, and clearing the cart. All actions are scoped to the currently
 * logged-in user to ensure cart data is secure and user-specific. The controller
 * relies on UserDao to identify the user, ProductDao to retrieve product data, and
 * ShoppingCartDao to manage cart persistence. Cross-origin requests are enabled to
 * support frontend applications.
 */
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

    /**
     * Retrieves the shopping cart for the currently authenticated user.
     * This method identifies the logged-in user using the security principal and returns
     * the shopping cart associated with that user. If the user cannot be found or an
     * error occurs while retrieving the cart, an appropriate HTTP error response is returned.
     */
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

    /**
     * Adds a product to the shopping cart for the currently authenticated user.
     * This method adds the specified product to the user’s shopping cart and returns
     * the updated cart. If the user or product cannot be found, or if an error occurs
     * during the add operation, an appropriate HTTP error response is returned.
     */
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

    /**
     * Updates an existing product in the shopping cart for the currently authenticated user.
     * This method updates the quantity of a product already present in the user’s cart
     * and returns the updated shopping cart. If the user cannot be found or an error
     * occurs during the update process, an appropriate HTTP error response is returned.
     */
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

    /**
     * Clears all products from the shopping cart for the currently authenticated user.
     * This method removes all items from the user’s shopping cart and returns an empty
     * cart. If the user cannot be found or an error occurs during the deletion process,
     * an appropriate HTTP error response is returned.
     */
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
