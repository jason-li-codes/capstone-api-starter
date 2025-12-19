package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.CategoryDao;
import org.yearup.models.Category;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MySQL implementation of the CategoryDao interface.
 * This data access object provides database operations for managing categories
 * using JDBC and a MySQL data source. It is responsible for retrieving, creating,
 * updating, and deleting category records in the database. The class extends a
 * shared MySqlDaoBase to obtain database connections and maps database rows to
 * Category model objects. This component is managed by Spring and used by higher
 * layers of the application to perform category-related persistence operations.
 */
@Component
public class MySqlCategoryDao extends MySqlDaoBase implements CategoryDao {

    // constructor injects the DataSource and passes it to the base DAO class
    public MySqlCategoryDao(DataSource dataSource) {
        super(dataSource);
    }

    /**
     * Retrieves all categories from the database.
     * This method queries the categories table and returns a list containing every
     * category found. Each database row is mapped to a Category object. If a database
     * error occurs during execution, a runtime exception is thrown.
     */
    @Override
    public List<Category> getAllCategories() {
        // list to store all categories
        List<Category> categories = new ArrayList<>();
        // SQL to select all categories
        String sql = """
                SELECT
                    *
                FROM
                    categories;""";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            // execute query
            ResultSet row = statement.executeQuery();
            // iterate through each row
            while (row.next()) {
                // map row to Category object
                Category category = mapCategoryRow(row);
                // add each category to list
                categories.add(category);
            }

        } catch (SQLException e) { // check for SQLException
            throw new RuntimeException(e);
        }
        // return list of categories
        return categories;
    }

    /**
     * Retrieves a single category by its unique identifier.
     * This method queries the categories table using the provided category ID and
     * returns the matching Category object if found. If no category exists with the
     * given ID, null is returned. Any database access errors result in a runtime exception.
     */
    @Override
    public Category getById(int categoryId) {
        // parameterized SQL to prevent SQL injection
        String sql = """
                SELECT
                    *
                FROM
                    categories
                WHERE
                    category_id = ?""";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            // set correct categoryId
            preparedStatement.setInt(1, categoryId);
            // execute query
            ResultSet resultSet = preparedStatement.executeQuery();
            // iterate through the row
            if (resultSet.next()) {
                // map row to Category object
                return mapCategoryRow(resultSet);
            }
        } catch (SQLException e) { // check for SQLException
            throw new RuntimeException(e);
        }
        // return null if not found
        return null;
    }

    /**
     * Creates a new category in the database.
     * This method inserts a new category record using the provided Category object.
     * After a successful insert, the generated category ID is retrieved and used to
     * return the fully populated Category object. If the insert fails, null is returned,
     * and database errors result in a runtime exception.
     */
    @Override
    public Category create(Category category) {
        // parameterized SQL to prevent SQL injection
        String sql = """
                INSERT INTO
                    categories (name, description)
                VALUES
                    (?, ?);""";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // set correct parameters
            statement.setString(1, category.getName());
            statement.setString(2, category.getDescription());
            // execute insert
            int rowsAffected = statement.executeUpdate();
            // check if insert succeeded
            if (rowsAffected > 0) {
                // get auto-incremented key
                ResultSet generatedKeys = statement.getGeneratedKeys();
                // retrieve generated key
                if (generatedKeys.next()) {
                    int categoryId = generatedKeys.getInt(1);
                    // return Category object
                    return getById(categoryId);
                }
            }
        } catch (SQLException e) { // check for SQLException
            throw new RuntimeException(e);
        }
        // return null if not found
        return null;
    }

    /**
     * Updates an existing category in the database.
     * This method updates the name and description of the category identified by the
     * given ID. If the category does not exist, no changes are made. Any database
     * access errors result in a runtime exception.
     */
    @Override
    public void update(int categoryId, Category category) {
        // parameterized SQL to prevent SQL injection
        String sql = """
                UPDATE
                    categories
                SET
                    name = ?,
                    description = ?
                WHERE
                    category_id = ?;""";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            // set correct parameters
            statement.setString(1, category.getName());
            statement.setString(2, category.getDescription());
            statement.setInt(3, categoryId);
            // execute update
            statement.executeUpdate();
        } catch (SQLException e) { // check for SQLException
            throw new RuntimeException(e);
        }
    }

    /**
     * Deletes a category from the database.
     * This method removes the category record associated with the given ID from the
     * database. If the category does not exist, no action is taken. Any database
     * access errors result in a runtime exception.
     */
    @Override
    public void delete(int categoryId) {
        // parameterized SQL to prevent SQL injection
        String sql = """
                DELETE FROM
                    categories
                WHERE
                    category_id = ?;""";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            // set correct parameters
            statement.setInt(1, categoryId);
            // execute update
            statement.executeUpdate();
        } catch (SQLException e) { // check for SQLException
            throw new RuntimeException(e);
        }
    }

    /**
     * Maps a database result set row to a Category object.
     * This helper method extracts category field values from the current row of the
     * result set and constructs a Category object. It is used internally by query
     * methods to convert database records into domain objects.
     */
    protected static Category mapCategoryRow(ResultSet row) throws SQLException {
        // get correct Category values by using getters for ResultSet
        int categoryId = row.getInt("category_id");
        String name = row.getString("name");
        String description = row.getString("description");
        // create and return Category based on parameters
        return new Category(categoryId, name, description);
    }

}
