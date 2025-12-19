package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.CategoryDao;
import org.yearup.data.ProductDao;
import org.yearup.models.Category;
import org.yearup.models.Product;

import java.util.List;

/**
 * Deletes a category by its ID.
 * This method responds to DELETE requests at the "/categories/{id}" endpoint and deletes the category with the specified ID.
 * This method requires the user to have the "ROLE_ADMIN" role to be executed successfully.
 * If there is an error during the category deletion process, a 500 Internal Server Error is thrown.
 */
@RestController
@RequestMapping("categories")
@CrossOrigin
public class CategoriesController {

    // add necessary daos
    private CategoryDao categoryDao;
    private ProductDao productDao;
    // create an Autowired controller to inject the categoryDao and ProductDao through constructor
    @Autowired
    public CategoriesController(CategoryDao categoryDao, ProductDao productDao) {
        this.categoryDao = categoryDao;
        this.productDao = productDao;
    }

    /**
     * Retrieves a list of all categories.
     * This method responds to GET requests at the "/categories" endpoint and returns all categories available in the database.
     * If there is an error during the retrieval process, a 500 Internal Server Error is thrown..
     */
    @GetMapping("")
    @PreAuthorize("permitAll()")
    // find and return all categories
    public List<Category> getAll() {

        try { // tries calling categoryDao
            return categoryDao.getAllCategories();
        } catch (Exception ex) { // returns error if dao fails
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    /**
     * Retrieves a category by its ID.
     * This method responds to GET requests at the "/categories/{id}" endpoint and retrieves a category by its ID.
     * If the category with the provided ID does not exist, a 404 Not Found status is returned.
     * If there is an error during the retrieval process, a 500 Internal Server Error is thrown.
     */
    @GetMapping("{id}")
    @PreAuthorize("permitAll()")
    public Category getById(@PathVariable int id) {
        // set category to null initially
        Category category = null;

        try {
            // try to get the correct category by calling getById from categoryDao
            category = categoryDao.getById(id);
        } // catches exception and throws error
        catch(Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
        // throws NOT_FOUND error if dao was successful but not category is found
        if (category == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return category;
    }

    /**
     * Retrieves a category by its ID.
     * This method responds to GET requests at the "/categories/{id}" endpoint and retrieves a category by its ID.
     * If the category with the provided ID does not exist, a 404 Not Found status is returned.
     * If there is an error during the retrieval process, a 500 Internal Server Error is thrown.
     */
    @GetMapping("{categoryId}/products")
    @PreAuthorize("permitAll()")
    public List<Product> getProductsById(@PathVariable int categoryId) {

        try { // calls productDao
            return productDao.listByCategoryId(categoryId);
        } catch (Exception ex) { // throws error if productDao fails
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    /**
     * Adds a new category.
     * This method responds to POST requests at the "/categories" endpoint and adds a new category to the database.
     * This method requires the user to have the "ROLE_ADMIN" role to be executed successfully.
     * If there is an error during the category creation process, a 500 Internal Server Error is thrown.
     */
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED) // outputs correct status
    @PreAuthorize("hasRole('ROLE_ADMIN')") // limits function to ADMIN
    public Category addCategory(@RequestBody Category category) {
        // try to call categoryDao to create Category object
        try {
            return categoryDao.create(category);
        } catch (Exception ex) { // throws error if categoryDao fails
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    /**
     * Updates an existing category by its ID.
     * This method responds to PUT requests at the "/categories/{id}" endpoint and updates an existing category with the provided ID.
     * This method requires the user to have the "ROLE_ADMIN" role to be executed successfully.
     * If the category with the specified ID does not exist or an error occurs during the update process, a 500 Internal Server Error is thrown.
     */
    @PutMapping("{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')") // limits function to ADMIN
    public Category updateCategory(@PathVariable int id, @RequestBody Category category) {
        // try to call categoryDao to update Category object
        try {
            categoryDao.update(id, category);
            // return Category after updating
            return categoryDao.getById(id);
        } catch (Exception ex) {  // throws error if categoryDao fails
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    /**
     * Deletes a category by its ID.
     * This method responds to DELETE requests at the "/categories/{id}" endpoint and deletes the category with the specified ID.
     * This method requires the user to have the "ROLE_ADMIN" role to be executed successfully.
     * If there is an error during the category deletion process, a 500 Internal Server Error is thrown.
     */
    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // output correct HTTP status
    @PreAuthorize("hasRole('ROLE_ADMIN')") // limits function to ADMIN
    public void deleteCategory(@PathVariable int id) {
        // try to call categoryDao to delete Category object
        try {
            categoryDao.delete(id);
        } catch (Exception ex) { // throws error if categoryDao fails
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }
}
