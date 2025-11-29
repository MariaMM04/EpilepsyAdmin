package org.example.JDBC.securitydb;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import Exceptions.RegisterError;
import encryption.PasswordHash;
import org.example.entities_securitydb.User; // Import User class
import ui.windows.NewPatientPanel;
import ui.windows.UserLogIn;


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
    public boolean insertUser(User user) throws RegisterError {
        String sql = "INSERT INTO Users (email, password, role_id, publicKey) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getPassword());
            ps.setInt(3, user.getRole_id());
            ps.setString(4,user.getPublicKey());
            ps.executeUpdate();
            System.out.println("User inserted successfully: " + user.getEmail());
            return true;
        } catch (SQLException e) {
            System.err.println("Error inserting user: " + e.getMessage());
            throw new RegisterError("Error inserting user: " + e.getMessage());
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
                        rs.getInt("role_id"),
                        rs.getString("publicKey"));
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
                        rs.getInt("role_id"),
                        rs.getString("publicKey"));
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
                        rs.getInt("role_id"),
                        rs.getString("publicKey")));
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
                if(active){
                    System.out.println("User activated successfully: " + email);
                }else {
                    System.out.println("User deactivated successfully: " + email);
                }
                //System.out.println("User " + (active ? "activated" : "deactivated") + ": " + email);
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
     *
     * @param user
     * @return
     * @throws RegisterError
     */
    public boolean register(User user) throws RegisterError {
        //Verification of email and password
        if (!UserLogIn.validatePassword(user.getPassword())){
            throw new RegisterError("Invalid password");
        } else if (!NewPatientPanel.validateEmail(user.getEmail())){
            throw new RegisterError("Invalid email");
        }else try {
            {
                //Hash the password for security in the database
                String hashedPassword = PasswordHash.generatePasswordHash(user.getPassword());
                User newUser = new User (user.getEmail(), hashedPassword,false);
                newUser.setRole_id(user.getRole_id());
                System.out.println("The user is: "+newUser.getEmail());
                System.out.println("The user's temporary password is: "+newUser.getPassword());
                newUser.setPublicKey(user.getPublicKey());
                System.out.println("The one-single-use token is: "+newUser.getPublicKey());
                return insertUser(newUser);
            }
        } catch (NoSuchAlgorithmException | InvalidKeySpecException message) {
            throw new RegisterError("User is already registered");
        }
    }

    /**
     * In the fisrt Log in it sets the active status to true and changes the PublicKey to the users one
     * @param u
     * @param providedPublicKey
     * @param newPublicKey
     * @return
     */
    public boolean firstLogin(User u, String providedPublicKey, String newPublicKey) {

        // 1. Check if it's actually first login
        if (u == null || u.isActive()) {
            return false; // not first login
        }
        // 2. Check if provided public key matches the one stored
        String storedPublicKey = u.getPublicKey();
        if (!storedPublicKey.equals(providedPublicKey)) {
            System.out.println("Public key does not match ");
            return false;
        }

        // 3. Change public key to the new one chosen by the user
        boolean updatedKey = changePublicKey(u, newPublicKey);

        // 4. Activate user
        boolean activated = updateUserActiveStatus(u.getEmail(), true);

        return updatedKey && activated;
    }


    /**
     * Retrieves a user from the DB with a given email and password. If the user does not exist or the password
     * does not match, it prints an error.
     *
     * @param email     the user's email
     * @param password  the user's password
     * @return          This {@code User} instance logged in
     */

    public User login(String email, String password) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            return null; // invalid input
        }
        String sql = ("SELECT * FROM users WHERE email = ?"); // By only fetching the email we can check if it is unique or not
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {

                //Verification if the password is the same as the hashedPassword in the DB
                String hashedPasswordDB = rs.getString("password");
                if (!PasswordHash.verifyPassword(password,hashedPasswordDB)){
                    System.out.println("Password does not match.");
                    return null;
                }

                User u = new User(
                        rs.getInt("id"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getBoolean("active"),
                        rs.getInt("role_id"),
                        rs.getString("publicKey")
                );
                u.setRole_id(rs.getInt("role_id"));
                return u;
            }
        } catch (Exception ex) {
            System.out.println("Log in failed: "+ex.getMessage());
            //throw new RuntimeException(ex);
            return null;
        }
        return null;
    }

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
     * @param u         the user that will have its password modified
     * @param password  the new password for the desired user
     * @return          boolean value of the performed modification. May be:
     *                  <code> true </code> if the password was successfully modified
     *                  <code> false </code> otherwise
     */
    public boolean changePassword(User u, String password) {
        if (u == null || u.getId() <= 0 || password == null || password.isBlank()) {
            return false;
        }
        try {
            //Hash the new password with PBKDF2
            String hashedPassword = PasswordHash.generatePasswordHash(password);

            String sql = "UPDATE users SET password = ? WHERE id = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {

                ps.setString(1, password);
                ps.setInt(2, u.getId());
                // to check if one row has been changed
                int row = ps.executeUpdate();
                return row == 1;
            } catch (SQLException e) {
                throw new RuntimeException(e); // return false
            }
        }catch (Exception e){
            System.out.println("Could not change password: "+e.getMessage());
        }
        return false;
    }

    /**
     * Makes the change of the given publicKey to your own publicKey
     * @param u
     * @param newPublicKey
     * @return
     */
    public boolean changePublicKey(User u, String newPublicKey) {
        // Basic validation
        if (u == null || u.getId() <= 0 || newPublicKey == null || newPublicKey.isBlank()) {
            return false;
        }

        try {
            String sql = "UPDATE Users SET publicKey = ? WHERE id = ?";

            try (PreparedStatement ps = connection.prepareStatement(sql)) {

                ps.setString(1, newPublicKey);
                ps.setInt(2, u.getId());

                int row = ps.executeUpdate();
                System.out.println("Public key updated for user: "+u.getEmail());
                return row == 1; // true if exactly one row was updated
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        } catch (Exception e) {
            System.out.println("Could not change public key: " + e.getMessage());
        }

        return false;
    }

}

