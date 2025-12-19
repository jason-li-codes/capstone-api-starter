package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.models.Category;
import org.yearup.models.Product;
import org.yearup.data.ProductDao;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST controller responsible for handling product-related API requests.
 * This controller provides endpoints for searching products using optional filter
 * criteria, retrieving individual products by ID, and performing create, update,
 * and delete operations on products. Read-only operations are publicly accessible,
 * while modification actions are restricted to users with the ADMIN role. The
 * controller relies on ProductDao for all product data access and allows cross-origin
 * requests to support frontend applications.
 */
@RestController
@RequestMapping("products")
@CrossOrigin
public class ProductsController {

    private ProductDao productDao;

    @Autowired
    public ProductsController(ProductDao productDao) {
        this.productDao = productDao;
    }

    /**
     * Searches for products using optional filter criteria.
     * This method allows clients to retrieve products based on category, price range,
     * and subcategory. Any combination of filters may be provided, and if no filters
     * are specified, all products are returned. If an error occurs during the search
     * process, an internal server error response is returned.
     */
    @GetMapping("")
    @PreAuthorize("permitAll()")
    public List<Product> search(@RequestParam(name="cat", required = false) Integer categoryId,
                                @RequestParam(name="minPrice", required = false) BigDecimal minPrice,
                                @RequestParam(name="maxPrice", required = false) BigDecimal maxPrice,
                                @RequestParam(name="subCategory", required = false) String subCategory) {

        try {
            return productDao.search(categoryId, minPrice, maxPrice, subCategory);
        } catch(Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    /**
     * Retrieves a product by its ID.
     * This method returns a single product that matches the provided ID. If the product
     * does not exist, a not found response is returned. If an error occurs while accessing
     * the data source, an internal server error response is returned.
     */
    @GetMapping("{id}")
    @PreAuthorize("permitAll()")
    public Product getById(@PathVariable int id) {

        Product product = null;

        try {
            product = productDao.getById(id);
        }
        catch(Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
        if (product == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return product;
    }

    /**
     * Creates a new product.
     * This method allows an administrator to add a new product to the system. The request
     * is restricted to users with the ADMIN role. If an error occurs during product
     * creation, an internal server error response is returned.
     */
    @PostMapping()
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Product addProduct(@RequestBody Product product) {

        try {
            return productDao.create(product);
        }
        catch(Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    /**
     * Updates an existing product.
     *
     * This method allows an administrator to modify an existing product identified by
     * its ID. After the update is performed, the updated product is returned. If an
     * error occurs during the update process, an internal server error response is
     * returned.
     */
    @PutMapping("{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Product updateProduct(@PathVariable int id, @RequestBody Product product) {

        try {
            productDao.update(id, product);
            return productDao.getById(id);
        }
        catch(Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    /**
     * Deletes a product by its ID.
     * This method allows an administrator to remove a product from the system. If the
     * deletion is successful, no content is returned. If an error occurs during the
     * deletion process, an internal server error response is returned.
     */
    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void deleteProduct(@PathVariable int id) {

        try {
            productDao.delete(id);
        }
        catch(Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }
}
