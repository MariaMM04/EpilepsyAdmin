package ui.windows;

import encryption.TokenUtils;
import org.example.entities_medicaldb.Doctor;
import org.example.entities_medicaldb.Patient;
import net.miginfocom.swing.MigLayout;
import org.example.entities_securitydb.Role;
import org.example.entities_securitydb.User;
import org.example.service.AdminLinkService;
import ui.components.*;

import java.util.Base64;
import java.util.List;
import javax.crypto.SecretKey;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Objects;
/**
 * Panel used to create and register a new patient in the Night Guardian
 * administrative system.
 * <p>
 * This form allows administrators to enter personal information, medical
 * assignment (doctor), login credentials, and demographic data, validating
 * the information before creating both a {@link Patient} and its associated
 * {@link User} account in the system.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Display editable fields for entering patient data</li>
 *     <li>Validate required fields, phone numbers, dates, email and password</li>
 *     <li>Allow assigning a doctor to the new patient</li>
 *     <li>Create the {@link User} and {@link Patient} in the database through
 *         {@link AdminLinkService}</li>
 *     <li>Display confirmation dialogs if leaving without saving</li>
 *     <li>Return to {@link MainMenu} after saving or canceling</li>
 * </ul>
 *
 * <h2>Lifecycle</h2>
 * <ol>
 *     <li>The constructor loads all available doctors from the database.</li>
 *     <li>{@link #initPatientInfo()} defines initial values and hints.</li>
 *     <li>{@link #initPatientForm()} constructs the UI layout.</li>
 *     <li>Admin fills out the form and presses:
 *         <ul>
 *             <li><b>Save</b> → triggers {@link #createPatientAndUser()}</li>
 *             <li><b>Cancel</b> → if unsaved, confirmation dialog is shown</li>
 *         </ul>
 *     </li>
 *     <li>If saved successfully, resets panel and returns to {@link MainMenu}.</li>
 * </ol>
 *
 * <h2>Validation Rules</h2>
 * <ul>
 *     <li><b>Date of Birth:</b> must follow YYYY-MM-DD</li>
 *     <li><b>Phone number:</b> 9 digits, numeric</li>
 *     <li><b>Email:</b> must be institutional: <code>@nightguardian.com</code></li>
 *     <li><b>Password:</b> minimum 8 characters, at least one digit</li>
 * </ul>
 *
 * @author MamenCortes
 * @author paulablancog
 */
public class NewPatientPanel extends JPanel implements ActionListener {
    private Application appMain;
    private JLabel nameHeading;
    private MyTextField name;
    private JLabel surnameHeading;
    private MyTextField surname;
    private JLabel emailHeading;
    private MyTextField email;
    private JLabel phoneHeading;
    private MyTextField phoneNumber;
    private JLabel genderHeading;
    private MyComboBox<String> gender;
    private MyComboBox<String> nextStep;
    private JLabel birthDateHeading;
    private MyTextField birthDate;
    private JLabel passwordHeading;
    private MyTextField password;
    private MyComboBox<String> doctors;
    private JLabel docsHeading;

    private JLabel title;
    protected String titleText = " ";
    protected JButton saveChangesBt;
    protected JButton cancelButton;
    protected JLabel errorMessage;
    protected JPanel formContainer;


    //Format variables: Color and Font
    private final Color titleColor = Application.dark_purple;
    private final Font titleFont = new Font("sansserif", Font.BOLD, 25);
    private final Font contentFont = new Font("sansserif", 1, 12);
    private final Color contentColor = Application.dark_turquoise;
    private Color textFieldBg = new Color(230, 245, 241);

    private Boolean saved;
    List<Doctor> docs;

    /**
     * Constructs the NewPatientPanel and initializes the form.
     *
     * @param appMain application controller used for navigation and DB access
     */
    public NewPatientPanel(Application appMain) {
        this.appMain = appMain;
        saved = false;
        initPatientInfo();

    }
    /**
     * Initializes the form fields, loads doctor list, defines hints, and enables editing.
     * Then delegates UI construction to {@link #initPatientForm()}.
     */
    public void initPatientInfo() {
        this.titleText = "Patient information";

        //Initialize values
        name = new MyTextField("Jane");
        name.setEnabled(true); //Doesnt allow editing

        surname = new MyTextField("Doe");
        surname.setEnabled(true);

        email = new MyTextField("jane.doe@nightguardian.com");
        email.setEnabled(true);

        phoneNumber = new MyTextField("612345678");
        phoneNumber.setEnabled(true);

        gender = new MyComboBox<String>();
        gender.addItem("Male");
        gender.addItem("Female");
        gender.addItem("Non-binary");

        birthDate = new MyTextField();
        birthDate.setHint("YYYY-MM-DD");
        birthDate.setEnabled(true);

        password = new MyTextField("password");
        formContainer = new JPanel();
        doctors = new MyComboBox<>();
        docs = appMain.doctorJDBC.getAllDoctors();
        for (Doctor doc : docs) {
            doctors.addItem(doc.getName()+" "+doc.getSurname()+"; "+doc.getSpeciality()+" specialist");
        }
        initPatientForm();
    }
    /**
     * Builds the patient creation form using MigLayout.
     * <p>
     * Includes:
     * <ul>
     *     <li>Personal fields (name, surname, gender, birth date)</li>
     *     <li>Contact fields (phone, email)</li>
     *     <li>Assigned doctor selection</li>
     *     <li>Password for system login</li>
     *     <li>Error message label</li>
     *     <li>Cancel and Save buttons</li>
     * </ul>
     */
    private void initPatientForm() {
        this.setLayout(new MigLayout("fill", "[][][][]", "[][][][][][][][][]push[]"));
        this.setBackground(Color.white);
        formContainer.setBackground(Color.white);
        formContainer.setLayout(new MigLayout("fill, inset 15, gap 5, wrap 2", "[50%][50%]", "[][][][][][][][]push"));

        //Add Title
        title = new JLabel(titleText);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setForeground(titleColor);
        title.setFont(titleFont);
        title.setAlignmentY(LEFT_ALIGNMENT);
        title.setIcon(new ImageIcon(getClass().getResource("/icons/patient-info64-2.png")));
        add(title, "cell 0 0 4 1, alignx left");
        add(formContainer,  "cell 0 1 4 7, grow, gap 10 10");

        //ROW 1
        //Name and surname
        nameHeading = new JLabel("Name*");
        nameHeading.setFont(contentFont);
        nameHeading.setForeground(contentColor);
        formContainer.add(nameHeading, "cell 0 0");

        surnameHeading = new JLabel("Surname*");
        surnameHeading.setFont(contentFont);
        surnameHeading.setForeground(contentColor);
        formContainer.add(surnameHeading, "grow");

        //ROW 2
        formContainer.add(name, "grow");
        formContainer.add(surname, "grow");

        //ROW 3
        genderHeading = new JLabel("Gender*");
        genderHeading.setFont(contentFont);
        genderHeading.setForeground(contentColor);
        formContainer.add(genderHeading, "grow");

        birthDateHeading = new JLabel("Date of Birth*");
        birthDateHeading.setFont(contentFont);
        birthDateHeading.setForeground(contentColor);
        formContainer.add(birthDateHeading, "grow");

        //ROW 4
        formContainer.add(gender, "grow");
        formContainer.add(birthDate,  "grow"); //TODO create birth date chooser

        //ROW 5
        phoneHeading = new JLabel("Phone Number*");
        phoneHeading.setFont(contentFont);
        phoneHeading.setForeground(contentColor);
        formContainer.add(phoneHeading, "grow");

        docsHeading = new JLabel("Assigned Doctor*");
        docsHeading.setFont(contentFont);
        docsHeading.setForeground(contentColor);
        formContainer.add(docsHeading, "grow");

        //ROW 6
        formContainer.add(phoneNumber, "grow");
        formContainer.add(doctors, "grow");

        //ROW 7
        emailHeading = new JLabel("Email*");
        emailHeading.setFont(contentFont);
        emailHeading.setForeground(contentColor);
        formContainer.add(emailHeading, "grow");

        passwordHeading = new JLabel("Password*");
        passwordHeading.setFont(contentFont);
        passwordHeading.setForeground(contentColor);
        formContainer.add(passwordHeading, "grow");

        //ROW 8
        formContainer.add(email, "grow");
        formContainer.add(password, "grow");

        errorMessage = new JLabel();
        errorMessage.setFont(new Font("sansserif", Font.BOLD, 12));
        errorMessage.setForeground(Color.red);
        errorMessage.setText("Error message test");
        this.add(errorMessage, "cell 0 8, span, center");
        errorMessage.setVisible(false);

        //Add buttons
        cancelButton = new MyButton("CANCEL", Application.turquoise, Color.white);
        cancelButton.addActionListener(this);
        add(cancelButton, "cell 0 9, span, split 2, center");

        saveChangesBt = new MyButton("SAVE AND GO BACK", Application.turquoise, Color.white);
        saveChangesBt.addActionListener(this);
        add(saveChangesBt, "center");
    }

    //TODO: Check validations
    /**
     * Creates both a {@link Patient} and a corresponding {@link User} based on the entered data.
     * <p>
     * Steps:
     * <ol>
     *     <li>Extract data from form fields</li>
     *     <li>Validate date of birth format</li>
     *     <li>Validate phone number (must be numeric 9-digit value)</li>
     *     <li>Validate email domain and format</li>
     *     <li>Validate password rules</li>
     *     <li>Assign selected doctor to the patient</li>
     *     <li>Create a {@link Role}="Patient"</li>
     *     <li>Create a {@link User} with credentials</li>
     *     <li>Persist patient + user using {@link AdminLinkService#createUserAndPatient(User, Patient)}</li>
     *     <li>If successful, reset the panel and return to main menu</li>
     * </ol>
     *
     * If any validation fails, an error message is shown and the user remains on this panel.
     */
    private void createPatientAndUser() {
        Patient p = new Patient();
        try {
            p.setName(name.getText());
            p.setSurname(surname.getText());
            p.setGender(gender.getSelectedItem().toString());
            p.setEmail(email.getText());
            p.setContact(phoneNumber.getText());
            p.setActive(true);
            LocalDate fecha;
            Integer phonenumber;

            //Validate Date of Birth
            try {
                if (!birthDate.getText().isEmpty()) {
                    fecha = LocalDate.parse(birthDate.getText());
                    p.setDateOfBirth(fecha);
                }
            } catch (DateTimeParseException exFecha) {
                showErrorMessage("Invalid date (use YYYY-MM-DD)");
                return;
            }

            //Validate Phone number
            try {
                if (!phoneNumber.getText().isEmpty() && phoneNumber.getText().length() == 9){
                    phonenumber = Integer.parseInt(phoneNumber.getText());
                    p.setContact(phonenumber.toString());
                }else {
                    showErrorMessage("Please, set a phone number of 9 digits");
                    return;
                }
            } catch (NumberFormatException exFecha) {
                showErrorMessage("The phone must be a numeric value");
                return;
            }

            //validate Email
            if(!validateEmail(p.getEmail())) {return;}

            //Validate password
            if(!validatePassword(password.getText())) {return;}

            //Create user
            SecretKey token = TokenUtils.generateToken();
            String oneTimeToken = Base64.getEncoder().encodeToString(token.getEncoded());
            Role role = appMain.securityManager.getRoleJDBC().findRoleByName("Patient");
            User u = new User(p.getEmail(), password.getText(), role.getId(), false);
            u.setPublicKey(oneTimeToken);
            //Assign Doctor
            int index = doctors.getSelectedIndex();
            p.setDoctorId(docs.get(index).getId());

            if (p.getName().isEmpty() || p.getSurname().isEmpty()|| p.getGender().isEmpty() || p.getEmail().isEmpty() || p.getContact().isEmpty() || password.getText().isEmpty()) {
                showErrorMessage("Please fill all the fields");
            }else {
                if(!appMain.adminLinkService.createUserAndPatient(u, p)){
                    showErrorMessage("Error creating user and patient");
                    saved = false;
                    return;
                }
                saved = true;
                showUserCredentials(appMain, u, oneTimeToken);
            }

        } catch (Exception ex) {
            showErrorMessage("Error creating user and patient");
        }
    }

    /**
     * Handles Save and Cancel button actions.
     * <ul>
     *     <li><b>Cancel</b>: If unsaved, confirmation dialog is shown; otherwise the panel resets and navigates back.</li>
     *     <li><b>Save</b>: Calls {@link #createPatientAndUser()}.</li>
     * </ul>
     *
     * @param e triggered action event
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == cancelButton) { //If they want to go back but didn't save the data, ask if they are sure they want to go back without saving
            if(!saved) {
                showQuestionPanel(appMain, "Are you sure you want to continue without saving changes?");
            }else{
                resetView();
                appMain.changeToMainMenu();
            }

        }else if(e.getSource() == saveChangesBt){
            createPatientAndUser();
        }
    }
    /**
     * Resets the form to its initial state:
     * <ul>
     *     <li>Clears all text fields</li>
     *     <li>Resets gender dropdown</li>
     *     <li>Hides error messages</li>
     *     <li>Marks panel as unsaved</li>
     * </ul>
     * Called after canceling or successfully saving.
     */
    private void resetView(){
        name.setText("");
        surname.setText("");
        email.setText("");
        phoneNumber.setText("");
        password.setText("");
        birthDate.setText("");
        gender.setSelectedIndex(0);
        errorMessage.setVisible(false);
        saved = false;
    }
    /**
     * Refreshes doctor list when the NewPatientPanel is revisited.
     * Useful when doctors have been added while the application is running.
     */
    public void updateView(){
        docs.clear();
        doctors.removeAllItems();
        docs = appMain.medicalManager.getDoctorJDBC().getAllDoctors();
        for (Doctor doc : docs) {
            doctors.addItem(doc.getName()+" "+doc.getSurname()+"; "+doc.getSpeciality()+" specialist");
        }
    }

    /**
     * Shows a confirmation dialog asking if the admin wants to leave without saving changes.
     *
     * @param parentFrame parent window reference for centering
     * @param question    the question to show in the dialog
     */
    private void showQuestionPanel(JFrame parentFrame, String question) {
        MyButton okButton = new MyButton("YES");
        MyButton cancelButton = new MyButton("CONTINUE EDITING");

        QuestionDialog panel = new QuestionDialog(question, okButton, cancelButton);
        panel.setPreferredSize(new Dimension(300, 150));
        JDialog dialog = new JDialog(parentFrame, "Are you sure?", true);
        dialog.getContentPane().add(panel);
        dialog.getContentPane().setBackground(Color.white);
        dialog.pack();
        dialog.setLocationRelativeTo(parentFrame);


        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetView();
                appMain.changeToMainMenu();
                dialog.dispose();
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
     * Validates password complexity.
     * <p>
     * Rules:
     * <ul>
     *     <li>Minimum 8 characters</li>
     *     <li>Must contain at least one digit</li>
     * </ul>
     *
     * @param password password to validate
     * @return true if valid, false otherwise
     */
    private Boolean validatePassword(String password) {
        boolean passwordVacia = (Objects.isNull(password)) || password.isEmpty();
        boolean goodPassword=false;
        System.out.println("password vacía "+passwordVacia);
        if(!passwordVacia && password.length() >= 8) {
            for(int i=0; i<password.length(); i++) {

                //The password must contain at least one number
                if(Character.isDigit(password.charAt(i))) {
                    goodPassword = true;
                }
            }
            if(!goodPassword) {
                showErrorMessage("The password must contain at least one number.");
                return false;
            }
        }else {
            showErrorMessage("Password's minimum lenght is of 8 characters");
            return false;
        }
        return true;

    }

    /**
     * Validates email format and domain.
     * The email must belong to the institutional domain:
     * <code>@nightguardian.com</code>.
     *
     * @param email email to validate
     * @return true if valid, false otherwise
     */
    public static Boolean validateEmail(String email) {
        if(!email.isBlank() && email.contains("@")) {
            String[] emailSplit = email.split("@");
            if(emailSplit.length >1 && emailSplit[1].equals("nightguardian.com")){
                return true;
            }
        }
        System.out.println("Invalid Email");
        return false;
    }

    /**
     * Displays an error message at the bottom of the form.
     *
     * @param message text to display in red
     */
    private void showErrorMessage(String message) {
        errorMessage.setText(message);
        errorMessage.setForeground(Color.RED);
        errorMessage.setVisible(true);
    }

    /**
     * Utility method to display the user credentials using a Night Guardian–styled window.
     *
     * @param parentFrame the parent frame for centering the dialog
     * @param user the user just registered
     */
    private void showUserCredentials(JFrame parentFrame, User user, String oneTimeToken) {
        JPanel panel = new JPanel();
        panel.setLayout(new MigLayout("wrap 2, fill, inset 15", "[30%][70%]", "push[][][][][]push"));
        panel.setBackground(Color.white);
        panel.setPreferredSize(new Dimension(350, 150));

        JLabel title = new JLabel("User Credentials");
        title.setFont(new Font("sansserif", 1, 20));
        title.setBackground(Color.white);
        title.setForeground(Application.dark_purple);
        panel.add(title, "span 2,alignx center");

        JLabel emailHeading = new JLabel("Email:");
        JLabel passwordHeading = new JLabel("Temporal Password:");
        JLabel tokenHeading = new JLabel("Single-Use Token:");
        emailHeading.setFont(contentFont);
        passwordHeading.setFont(contentFont);
        tokenHeading.setFont(contentFont);
        emailHeading.setForeground(Application.dark_turquoise);
        passwordHeading.setForeground(Application.dark_turquoise);
        tokenHeading.setForeground(Application.dark_turquoise);

        JLabel email = new JLabel(user.getEmail());
        JLabel password = new JLabel(user.getPassword());
        JLabel token = new JLabel(oneTimeToken);
        email.setFont(contentFont);
        password.setFont(contentFont);
        token.setFont(contentFont);
        email.setForeground(Color.gray);
        password.setForeground(Color.gray);
        token.setForeground(Color.gray);

        panel.add(emailHeading);
        panel.add(email);
        panel.add(passwordHeading);
        panel.add(password);
        panel.add(tokenHeading);
        panel.add(token);

        MyButton okButton = new MyButton("OK", Application.turquoise, Color.white);
        panel.add(okButton, "center, span 2");

        JDialog dialog = new JDialog(parentFrame, "Message dialog", true); //dont allow interacting with other panels at the same time
        dialog.getContentPane().add(panel);
        dialog.getContentPane().setBackground(Color.white);
        dialog.pack();
        dialog.setLocationRelativeTo(parentFrame);

        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetView();
                dialog.dispose();
                appMain.changeToMainMenu();
            }
        });

        dialog.setVisible(true);
    }
}