package org.example.JDBC.securitydb;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import encryptation.RSAKeyManager;
import encryptation.RSAUtil;
import org.example.entities_securitydb.User; // Import User class


/**
 * The {@code UserJDBC} class is a Data Access Object class responsible for all JDBC operations related to the
 * {@code Users} table. This class is typically used by {@link SecurityManager} to perform operations
 * over the security database and to provide the active Connection which is shared by all methods in this DAO
 *
 * @author MariaMM04
 * @author Pblan
 * @author MamenCortes
 */
public class UserJDBC {

    private static Connection connection;

    public UserJDBC(Connection connection) throws SQLException {
        this.connection = connection;
    }

    /**
     * Inserts an existing {@code User} into the medical database {@code medicaldb} by a SQL query specified
     * inside the method
     *
     * @param user      An existing user
     * @return          boolean value of the performed insertion. May be:
     *                  <code> true </code> if the user was successfully inserted into the database
     *                  <code> false </code> otherwise
     */
    public boolean insertUser(User user) {
        String sql = "INSERT INTO Users (email, password, role_id) VALUES (?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getPassword());
            ps.setInt(3, user.getRole_id());
            ps.executeUpdate();
            System.out.println("User inserted successfully: " + user.getEmail());
            return true;
        } catch (SQLException e) {
            System.err.println("Error inserting user: " + e.getMessage());
            return false;
        }
    }
    /**
     * Retrieves {@code User} by its email address from the medical database by a SQL query.
     *
     * @param email     the desired user's we want to retrieve email address
     * @return          the desired user we want to retrieve
     */
    public User findUserByEmail(String email) {
        String sql = "SELECT * FROM Users WHERE email = ?";
        User user = null;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                user = new User(
                        rs.getInt("id"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getBoolean("active"),
                        rs.getInt("role_id"));
                System.out.println("User found: " + email);
            } else {
                System.out.println("No user found with email: " + email);
            }

            rs.close();
        } catch (SQLException e) {
            System.err.println("Error finding user: " + e.getMessage());
        }

        return user;
    }

    /**
     * Retrieves {@code User} by its unique identifier (id) from the security database by a SQL query.
     *
     * @param id     the unique identifier of the user we want to retrieve
     * @return       the desired user we want to retrieve
     */
    public User findUserByID(int id) {
        String sql = "SELECT * FROM Users WHERE id = ?";
        User user = null;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                user = new User(
                        rs.getInt("id"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getBoolean("active"),
                        rs.getInt("role_id"));
                System.out.println("User found: " + id);
            } else {
                System.out.println("No user found with ID: " + id);
            }

            rs.close();
        } catch (SQLException e) {
            System.err.println("Error finding user: " + e.getMessage());
        }

        return user;
    }

    /**
     * Retrieves all {@code User} instances stored in the security database by a SQL query.
     *
     * @return  A list of all the users inside the medical database
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM Users";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                users.add(new User(
                        rs.getInt("id"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getBoolean("active"),
                        rs.getInt("role_id")));
            }
            System.out.println("Retrieved " + users.size() + " users.");

        } catch (SQLException e) {
            System.err.println("Error retrieving users: " + e.getMessage());
        }

        return users;
    }

    /**
     * Updates the active status of the {@code User} instance with the corresponding email.
     *
     * @param email     the email of the updated user.
     * @param active    the user's logical flag
     * @return          boolean value of the performed update. May be:
     *                  <code> true </code> if the user was successfully updated into the database
     *                  <code> false </code> otherwise
     */
    public boolean updateUserActiveStatus(String email, boolean active) {
        String sql = "UPDATE users SET active = ? WHERE email = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setString(2, email);
            int affected = ps.executeUpdate();

            if (affected > 0) {
                System.out.println("User " + (active ? "activated" : "deactivated") + ": " + email);
                return true;
            } else {
                System.out.println("No user found to update: " + email);
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Error updating user active status: " + e.getMessage());
            return false;
        }
    }

    /**
     * Registers a new user in the database after performing password validation.
     *
     * @param email     the new user's email
     * @param password  the new user's password
     * @param active    the new users active flag. May be:
     *                  <code> true </code> if the user was successfully updated into the database
     *                  <code> false </code> otherwise
     * @return          boolean value of the performed registration. May be:
     *                  <code> true </code> if the user was successfully registered into the database
     *                  <code> false </code> otherwise
     */
    public boolean register(String email, String password, boolean active) {

        if (email.isBlank() || email == null || password.isBlank() || password == null || active == true) { // If the email or password are empty do not create
            return false;
        } else if (isUser(email)) {
            return false;
        } else try {
            {
                // Retrieve public key to encrypt the password introduced by the user
                PublicKey publicKey = RSAKeyManager.retrievePublicKey("admin"); //Assume that the file containing the PB is "admin_public_key"
                String encryptedPassword = RSAUtil.encrypt(password, publicKey);
                User newUser = new User (email, encryptedPassword,true);
                return insertUser(newUser);
            }
        } catch (Exception e) {
            System.out.println("Register failed: "+e.getMessage());
            return false;
        }
    }


    /**
     * Retrieves a user from the DB with a given email and password. If the user does not exist or the password
     * does not match, it prints an error.
     *
     * @param email     the user's email
     * @param password  the user's password
     * @return          This {@code User} instance logged in
     */

    //TODO: comprobar que la contraseña proporcionada coincide con la de la base de datos
    public User login(String email, String password) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            return null; // invalid input
        }
        String sql = ("SELECT * FROM users WHERE email = ?"); // By only fetching the email we can check if it is unique or not
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String encryptedPassword = rs.getString("password");
                //Decrypt the password
                PrivateKey privateKey = RSAKeyManager.retrievePrivateKey("admin"); //Assuming that the file containing the PK: "admin_private_key"
                String decryptedPassword = RSAUtil.decrypt(encryptedPassword, privateKey);

                //Verify if the password exists in the database
                if (!password.equals(decryptedPassword)){
                    System.out.println("Password does not match.");
                    return null;
                }

                User u = new User(
                        rs.getInt("id"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getBoolean("active"),
                        rs.getInt("role_id")
                );
                u.setRole_id(rs.getInt("role_id"));
                return u;
            }
        } catch (Exception ex) {
            System.out.println("Log in failed: "+ex.getMessage());
            throw new RuntimeException(ex);
        }
        return null;
    }

    /**
     * Checks if a user exists by a given email
     */

    /**
     * Checks if a user exists by a given email inside the {@code securitydb} database
     * @param email     the email of the desired user
     * @return          boolean value of the performed login. May be:
     *                  <code> true </code> if the user was successfully logged into the database
     *                  <code> false </code> otherwise
     */
    public boolean isUser(String email) {
        if (email == null || email.isBlank()){
            return false;// invalid input
        }

        String sql = ("SELECT * FROM users WHERE email = ? ");
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            return rs.next(); //returns true if rs.next() exists
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * Changes the password of the given user of the database.
     *
     * @param u         the user that will have its password modified
     * @param password  the new password for the desired user
     * @return          boolean value of the performed modification. May be:
     *                  <code> true </code> if the password was successfully modified
     *                  <code> false </code> otherwise
     */
    public boolean changePassword(User u, String password) {
        if (u==null || u.getId() <= 0 || password == null || password.isBlank()){
            return false;
        }
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)){
            ps.setString(1, password);
            ps.setInt(2, u.getId());
            // to check if one row has been changed
            int row = ps.executeUpdate();
            return row == 1;
        } catch (SQLException e) {
            throw new RuntimeException(e); // return false
        }
    }
}

