package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.OrderDao;
import org.yearup.data.UserDao;
import org.yearup.models.Order;
import org.yearup.models.User;
import org.yearup.services.OrderService;

import java.security.Principal;
import java.util.List;

/**
 * REST controller responsible for handling order-related API requests.
 * This controller provides endpoints for authenticated users to view their orders
 * and to complete the checkout process. It uses the authenticated user’s security
 * principal to ensure that users can only access and create orders associated with
 * their own account. The controller coordinates with OrderDao for order retrieval,
 * UserDao for user validation, and OrderService to handle checkout business logic.
 * Cross-origin requests are allowed to support frontend applications.
 */
@RestController
@CrossOrigin
@RequestMapping("orders")
public class OrdersController {
    // this controller requires the following classes
    private OrderDao orderDao;
    private OrderService orderService;
    private UserDao userDao;

    @Autowired
    // constructor that injects necessary dependencies
    public OrdersController(OrderDao orderDao, OrderService orderService, UserDao userDao) {
        this.orderDao = orderDao;
        this.orderService = orderService;
        this.userDao = userDao;
    }

    /**
     * Retrieves all orders associated with the currently authenticated user.
     * This method uses the authenticated user’s security principal to identify the user
     * making the request and returns a list of orders that belong only to that user.
     * If the user cannot be found or an error occurs while retrieving the orders,
     * an appropriate HTTP error response is returned.
     */
    @GetMapping("view")
    @PreAuthorize("isAuthenticated()") // check to ensure correct user is accessing method
    public List<Order> getOrders(Principal principal) {

        // try to execute code to retrieve orders
        try {
            // first get the user by accessing principal and getting the name attribute
            User user = userDao.getUserByUserName(principal.getName());
            // if no user is found, throw NOT_FOUND error
            if (user == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            // otherwise, retrieve userId
            int userId = user.getId();
            // use userId to call orderDao and return list of orders
            return orderDao.getOrdersByUserId(userId);

        } catch (Exception ex) { // throw internal server error if dao fails
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    /**
     * Processes the checkout operation for the currently authenticated user.
     * This method creates a new order based on the authenticated user’s current cart
     * and completes the checkout process using the order service. The request is
     * restricted to authenticated users, and only orders associated with the current
     * user may be created. If checkout fails or an error occurs during processing,
     * an appropriate HTTP error response is returned.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // output correct HTTP status
    @PreAuthorize("isAuthenticated()") // check to ensure correct user is accessing method
    public Order checkout(Principal principal) {
        // try to execute code to retrieve orders
        try {
            // first get the user by accessing principal and getting the name attribute
            User user = userDao.getUserByUserName(principal.getName());
            // if no user is found, throw NOT_FOUND error
            if (user == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            // otherwise, retrieve userId
            int userId = user.getId();
            // create order by calling orderService and entering correct userId
            Order order = orderService.checkout(userId);
            // if order is null, throw appropriate HTTP exception
            if (order == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Checkout failed.");
            return order; // return created order
        } catch (ResponseStatusException e) {
            throw e; // throw response exception if dao fails
        } catch (Exception e) { // throw server exception for all other errors
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Server not connected.");
        }
    }
}