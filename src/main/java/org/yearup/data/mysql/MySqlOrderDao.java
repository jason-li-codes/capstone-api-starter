package org.yearup.data.mysql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yearup.data.OrderDao;
import org.yearup.models.Order;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * MySQL implementation of the OrderDao interface.
 * This data access object provides database operations for managing orders
 * using JDBC and a MySQL data source. It is responsible for creating new orders
 * and retrieving orders associated with a specific user. The class extends
 * MySqlDaoBase to obtain database connections and maps database rows to Order
 * model objects. This component is managed by Spring and used by higher layers
 * of the application to perform order-related persistence operations.
 */
@Component
public class MySqlOrderDao extends MySqlDaoBase implements OrderDao {
    // constructor injects the datasource and passes it to the base DAO
    public MySqlOrderDao(DataSource dataSource) {
        super(dataSource);
    }

    /**
     * Creates a new order in the database.
     * This method inserts a new order record using the provided Order object. The
     * order's fields, including user ID, date, address, city, state, zip code, and
     * shipping amount, are saved. After a successful insert, the generated order ID
     * is retrieved and set on the Order object, which is then returned. Any database
     * errors result in a runtime exception.
     */
    @Override
    public Order create(Order order) {
        // parameterized SQL to prevent SQL injection
        String sql = """
                INSERT INTO
                    orders (user_id, date, address, city, state, zip, shipping_amount)
                VALUES
                    (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            // set parameters for the insert statement
            preparedStatement.setInt(1, order.getUserId());
            preparedStatement.setTimestamp(2, java.sql.Timestamp.valueOf(order.getDate()));
            preparedStatement.setString(3, order.getAddress());
            preparedStatement.setString(4, order.getCity());
            preparedStatement.setString(5, order.getState());
            preparedStatement.setString(6, order.getZip());
            preparedStatement.setBigDecimal(7, order.getShippingAmount());
            // execute insert and check if any row was affected
            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows == 0) throw new SQLException("Creating order failed, no rows affected.");
            // retrieve generated order id
            try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    order.setOrderId(resultSet.getInt(1));
                }
            }
            // return the order object with generated id
            return order;
        } catch (Exception e) { // wrap exception
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves all orders for a specific user.
     * This method queries the orders table for records associated with the provided
     * user ID. Each row is mapped to an Order object, including converting the
     * datetime string to a LocalDateTime object. A list of all orders for the user
     * is returned. Any database access errors result in a runtime exception.
     */
    @Override
    public List<Order> getOrdersByUserId(int userId) {
        // instantiate list to store orders
        List<Order> orders = new ArrayList<>();
        // parameterized SQL to prevent SQL injection
        String sql = """
                SELECT
                    *
                FROM
                    orders
                WHERE
                    user_id = ?;""";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            // set userId parameter
            statement.setInt(1, userId);
            // execute query
            ResultSet row = statement.executeQuery();
            // iterate through result set and map to Order objects
            while (row.next()) {
                Order order = new Order();
                order.setOrderId(row.getInt(1));
                order.setUserId(row.getInt(2));
                // parse datetime string from sql into LocalDateTime
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                order.setDate(LocalDateTime.parse(row.getString(3), formatter));

                order.setAddress(row.getString(4));
                order.setCity(row.getString(5));
                order.setState(row.getString(6));
                order.setZip(row.getString(7));
                order.setShippingAmount(row.getBigDecimal(8));
                // add order to list
                orders.add(order);
            }
        } catch (SQLException e) { // wrap exception
            throw new RuntimeException(e);
        }
        // return list of orders
        return orders;
    }

}
