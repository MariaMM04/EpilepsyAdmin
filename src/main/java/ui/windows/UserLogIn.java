package ui.windows;

import net.miginfocom.swing.MigLayout;
import ui.components.*;
import org.example.entities_securitydb.*;
import org.example.JDBC.securitydb.UserJDBC;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;
/**
 * Panel responsible for handling user authentication into the application.
 * <p>
 * This view appears at startup and whenever the user logs out. It provides:
 * </p>
 * <ul>
 *     <li>Email and password input fields</li>
 *     <li>A "Forgot your password?" workflow</li>
 *     <li>Error feedback for invalid login attempts</li>
 * </ul>
 *
 * <h3>Lifecycle</h3>
 * <ul>
 *     <li>The panel is created once during {@link Application} initialization.</li>
 *     <li>It remains persistent as part of the application panel stack.</li>
 *     <li>Before returning to this panel, the application calls {@link #resetPanel()}
 *         to clear all fields and messages.</li>
 * </ul>
 *
 * @author MamenCortes
 * @author paulablancog
 */
public class UserLogIn extends JPanel implements ActionListener {

    private static final long serialVersionUID = 1L;
    private JPanel panelLogIn;
    private MyButton applyLogIn;
    private MyButton changePassword;
    private Application appMenu;
    private MyTextField emailTxF;
    private MyTextField passwordTxF;
    private MyTextField emailTxFLogIn;
    private MyTextField passwordTxFLogIn;
    private JPanel coverPanel;
    private JLabel errorMessage;
    private JLabel errorMessage2;

    /**
     * Creates the login panel, sets the main layout, initializes all required UI
     * components and builds both the login form and the graphical cover panel.
     *
     * @param appMenu reference to the central {@link Application} controller
     */
    public UserLogIn(Application appMenu) {
        this.appMenu = appMenu;
        this.setLayout(new MigLayout("fill, inset 0, gap 0", "[30]0px[70:pref]", "[]"));
        init();
    }

    /**
     * Initializes all UI elements used in the login screen, including:
     * <ul>
     *     <li>Buttons (Log In, Forgot Password)</li>
     *     <li>Text fields for email and password</li>
     *     <li>The gradient cover panel with the application logo</li>
     *     <li>The login form panel</li>
     * </ul>
     * <p>
     * The actual content of the login form is built in {@link #initLogin()}.
     * </p>
     */
    private void init() {
        //Initialize buttons
        applyLogIn = new MyButton();
        applyLogIn.addActionListener(this);
        changePassword = new MyButton();
        changePassword.addActionListener(this);

        emailTxF = new MyTextField();
        emailTxF.addActionListener(this);
        passwordTxF = new MyTextField();
        passwordTxF.addActionListener(this);
        emailTxFLogIn = new MyTextField();
        emailTxFLogIn.addActionListener(this);
        passwordTxFLogIn = new MyTextField();
        passwordTxFLogIn.addActionListener(this);

        //Log In panel
        errorMessage2 = new JLabel();
        errorMessage = new JLabel();
        panelLogIn = new JPanel();
        panelLogIn.setOpaque(true);
        initLogin();
        //Cover panel
        coverPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g); // pinta el fondo base

                Graphics2D g2d = (Graphics2D) g.create();
                int width = getWidth();
                int height = getHeight();

                // 🔹 Degradado de izquierda a derecha (puedes cambiarlo a vertical si quieres)
                GradientPaint gradient = new GradientPaint(0, 0, Application.light_purple, 0, getHeight(), Application.light_turquoise);

                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, width, height);
                g2d.dispose();
            }
        };
        coverPanel.setOpaque(false);
        coverPanel.setLayout(new MigLayout("wrap, fill", "[center]", "push[]10[]10[]push"));
        JLabel picLabel = new JLabel();
        picLabel.setIcon(new ImageIcon(UserLogIn.class.getResource("/icons/night_guardian_256.png")));
        coverPanel.add(picLabel);

        this.add(coverPanel, "grow");
        this.add(panelLogIn, "grow");

    }

    /**
     * Builds the login form panel: email input, password input, and buttons.
     * <p>
     * Sets styles, icons, and attaches action listeners to the main user inputs.
     * Error message labels are initialized but hidden by default.
     * </p>
     */
    public void initLogin() {

        panelLogIn.setBackground(Color.white);
        panelLogIn.setLayout(new MigLayout("wrap", "push[center]push", "push[]25[]10[]10[][]15[]push"));
        JLabel label = new JLabel("Log In");
        label.setFont(new Font("sansserif", 1, 30));
        label.setForeground(Application.dark_purple);
        panelLogIn.add(label);

        emailTxFLogIn.setPrefixIcon(new ImageIcon(getClass().getResource("/icons/mail.png")));
        emailTxFLogIn.setHint("Email");
        panelLogIn.add(emailTxFLogIn, "w 60%");

        passwordTxFLogIn.setPrefixIcon(new ImageIcon(getClass().getResource("/icons/pass.png")));
        passwordTxFLogIn.setHint("Password");
        panelLogIn.add(passwordTxFLogIn, "w 60%");

        changePassword.setText("Forgot your password ?");
        changePassword.setFont(new Font("sansserif", 1, 12));
        changePassword.setContentAreaFilled(false);
        changePassword.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panelLogIn.add(changePassword);

        errorMessage2 = new JLabel();
        errorMessage2.setFont(new Font("sansserif", Font.BOLD, 12));
        errorMessage2.setForeground(Color.red);
        errorMessage2.setText("Error message test");
        errorMessage2.setVisible(false);
        panelLogIn.add(errorMessage2);

        applyLogIn.setText("LOG IN");
        applyLogIn.setBackground(Application.turquoise);
        applyLogIn.setForeground(Color.white);
        applyLogIn.setUI(new StyledButtonUI());
        panelLogIn.add(applyLogIn, "w 40%, h 40");
    }

    /**
     * Handles button interactions:
     * <ul>
     *     <li><b>LOG IN:</b> Attempts authentication via {@link #logIn()}.
     *         On success, resets the panel and transitions to the main menu.</li>
     *     <li><b>Forgot password:</b> Validates the email field and opens a
     *         password change dialog if allowed.</li>
     * </ul>
     *
     * @param e the action event triggered by the user
     */
    @Override
    public void actionPerformed(ActionEvent e) {

        if(e.getSource() == applyLogIn) {
            System.out.println("LogIn");
            if(logIn()) {
                appMenu.changeToMainMenu();
                resetPanel();
            }

        }else if(e.getSource() == changePassword) {
            if(canChangePassword()) {
                showChangePasswordPane(appMenu);
            }

        }
    }

    /**
     * Displays the "Change Password" dialog, where the user can enter a new
     * password twice for confirmation. Validation is handled inside this method.
     *
     * @param parentFrame the application frame to anchor the dialog
     */
    private void showChangePasswordPane(JFrame parentFrame) {
        String emailString = emailTxFLogIn.getText();
        MyTextField password1 = new MyTextField();
        MyTextField password2 = new MyTextField();
        MyButton okButton = new MyButton("Aceptar");
        MyButton cancelButton = new MyButton("Cancelar");

        ChangePassword panel = new ChangePassword(password1, password2, okButton, cancelButton);
        panel.setBackground(Color.white);
        panel.setPreferredSize(new Dimension(400, 300));

        JDialog dialog = new JDialog(parentFrame, "Change Password", true);
        dialog.getContentPane().add(panel);
        dialog.getContentPane().setBackground(Color.white);
        dialog.pack();
        dialog.setLocationRelativeTo(parentFrame);

        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String pass1 = password1.getText();
                String pass2 = password2.getText();
                if(pass1 != null && pass1.equals(pass2) && !pass1.isBlank()) {
                    if(validatePassword(pass2)) {
                        //TODO: call JDBC tochangePassword
                        /*User u = appMenu.jpaUserMan.getUserByEmail(emailString);
                        if(!appMenu.jpaUserMan.changePassword(u, pass2)) {
                            showErrorMessage("Password could't be changed");
                        }
                        dialog.dispose();*/
                        panel.showErrorMessage("Password validated");
                    }else {
                        panel.showErrorMessage("Password must contain 1 number and minimum 8 characters");
                    }

                }else{
                    panel.showErrorMessage("Passwords do not match");
                }

            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });

        dialog.setVisible(true);
    }

    /**
     * Attempts to authenticate the user with the server using the e-mail and
     * password fields.
     * <p>
     * On success, returns {@code true}. On failure, an error message is shown
     * and {@code false} is returned.
     * </p>
     *
     * @return {@code true} if authentication succeeded, otherwise {@code false}
     */
    private Boolean logIn() {
        String email = emailTxFLogIn.getText();
        String password = passwordTxFLogIn.getText();
        System.out.println("email: " + email+" password: "+password);
        if(!email.isBlank() && !password.isBlank()) {
            User user = appMenu.userJDBC.login(email, password);
            System.out.println(user);

            //User is null if it doesn't exist
            if(user != null) {
                Role role = appMenu.securityManager.getRoleJDBC().findRoleByID(user.getRole_id());
                if(role.getRolename().equals("Administrator")) {
                    appMenu.user = user;
                    return true;
                }else{
                    showErrorMessage("Unauthorized access");
                    return false;
                }

            }else {
               showErrorMessage("Invalid user or password");
                return false;
            }

        }else {
            showErrorMessage("Complete all fields");
            return false;
        }
    }

    /**
     * Determines whether the password change dialog can be opened.
     * <p>
     * This method first checks if the email introduced is of a real user.
     * If not, it returns false and doesn't allow to change the password.
     * </p>
     *
     * @return {@code true} if password change is allowed
     */
    public Boolean canChangePassword() {
        String email = emailTxFLogIn.getText();
        if(email != null && !email.isBlank()){
            Boolean isUser = appMenu.userJDBC.isUser(email);
            if(isUser) {
                return true;
            }else {
                showErrorMessage("Invalid user or password");
                return false;
            }
        }else {
            showErrorMessage("Write the email first");
            return false;
        }
    }

    /**
     * Validates password complexity: at least 8 characters and at least one digit.
     *
     * @param password the password to validate
     * @return {@code true} if the password meets requirements
     */
    public static Boolean validatePassword(String password) {
        boolean passwordVacia = (Objects.isNull(password)) || password.isEmpty();
        boolean goodPassword=false;
        System.out.println("empty password "+passwordVacia);
        if(!passwordVacia && password.length() >= 8) {
            for(int i=0; i<password.length(); i++) {

                //The password must contain at least one number
                if(Character.isDigit(password.charAt(i))) {
                    goodPassword = true;
                }
            }
            if(!goodPassword) {
                System.out.println("The password must contain at least one number.");
                return false;
            }
        }else {
            System.out.println("Password's minimum length is of 8 characters");
            return false;
        }
        return true;

    }

    /**
     * Resets email, password, and error fields.
     * <p>
     * Called automatically after login or when returning to the login screen.
     * </p>
     */
    private void resetPanel() {
        emailTxF.setText(null);
        emailTxFLogIn.setText(null);
        passwordTxF.setText(null);
        passwordTxFLogIn.setText(null);
        hideErrorMessage();
    }

    /**
     * Shows a login-related error message below the login form.
     *
     * @param text the message to show
     */
    public void showErrorMessage(String text){
        errorMessage.setVisible(true);
        errorMessage.setText(text);
        errorMessage2.setVisible(true);
        errorMessage2.setText(text);
    }

    /**
     * Hides the login error message.
     */
    public void hideErrorMessage() {
        errorMessage.setVisible(false);
        errorMessage2.setVisible(false);
    }

}
