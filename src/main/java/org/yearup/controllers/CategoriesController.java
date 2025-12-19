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

// add the annotations to make this a REST controller
// add the annotation to make this controller the endpoint for the following url
// http://localhost:8080/categories
// add annotation to allow cross site origin requests
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

    // add the appropriate annotation for a get action
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

    @GetMapping("{id}")
    @PreAuthorize("permitAll()")
    // get the category by id
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

    // the url to return all products in category 1 would look like this
    // https://localhost:8080/categories/1/products
    @GetMapping("{categoryId}/products")
    @PreAuthorize("permitAll()")
    // get a list of product by categoryId
    public List<Product> getProductsById(@PathVariable int categoryId) {

        try { // calls productDao
            return productDao.listByCategoryId(categoryId);
        } catch (Exception ex) { // throws error if productDao fails
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED) // outputs correct status
    @PreAuthorize("hasRole('ROLE_ADMIN')") // limits function to ADMIN
    // insert the category
    public Category addCategory(@RequestBody Category category) {
        // try to call categoryDao to create Category object
        try {
            return categoryDao.create(category);
        } catch (Exception ex) { // throws error if categoryDao fails
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    @PutMapping("{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')") // limits function to ADMIN
    // update the category by id
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

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // output correct HTTP status
    @PreAuthorize("hasRole('ROLE_ADMIN')") // limits function to ADMIN
    // delete the category by id
    public void deleteCategory(@PathVariable int id) {
        // try to call categoryDao to delete Category object
        try {
            categoryDao.delete(id);
        } catch (Exception ex) { // throws error if categoryDao fails
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }
}
