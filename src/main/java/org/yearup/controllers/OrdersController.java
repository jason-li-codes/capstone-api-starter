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

// add the annotations to make this a REST controller
// add the annotation to make this controller the endpoint for the following url
// http://localhost:8080/orders
// add annotation to allow cross site origin requests
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