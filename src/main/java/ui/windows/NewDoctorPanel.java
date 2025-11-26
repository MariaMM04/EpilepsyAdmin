package ui.windows;

import net.miginfocom.swing.MigLayout;
import org.example.entities_medicaldb.Doctor;
import org.example.entities_securitydb.Role;
import org.example.entities_securitydb.User;
import org.example.service.AdminLinkService;
import ui.components.MyButton;
import ui.components.MyComboBox;
import ui.components.MyTextField;
import ui.components.QuestionDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;

/**
 * Panel used to create and register a new physician (Doctor) in the Night Guardian
 * administrative system.
 * <p>
 * This view allows the administrator to enter personal information, contact
 * details, specialty, department, and to create a corresponding {@link User}
 * account that will be stored in the security database.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Display and validate doctor data entry fields</li>
 *     <li>Validate email and password formats</li>
 *     <li>Create associated {@link User} and {@link Doctor} objects</li>
 *     <li>Use {@link AdminLinkService} to store the new doctor in the system</li>
 *     <li>Navigate back to the main menu, with or without saving</li>
 * </ul>
 *
 * <h2>Lifecycle</h2>
 * <ol>
 *     <li>The constructor initializes empty fields with placeholder hints.</li>
 *     <li>{@link #initDoctorInfo()} prepares the UI values and delegates to {@link #initDoctorForm()}.</li>
 *     <li>The administrator enters data and presses:
 *         <ul>
 *             <li><b>Save</b> → calls {@link #createDoctorAndUSer()}</li>
 *             <li><b>Cancel</b> → if unsaved, asks for confirmation</li>
 *         </ul>
 *     </li>
 *     <li>If saved successfully, the panel resets and returns to {@link MainMenu}.</li>
 * </ol>
 *
 * <h2>User Account Creation Process</h2>
 * <ol>
 *     <li>Validate doctor fields</li>
 *     <li>Validate phone number (numeric, 9 digits)</li>
 *     <li>Validate email format (@nightguardian.com)</li>
 *     <li>Validate password (≥8 characters, contains digit)</li>
 *     <li>Create {@link Role} "Doctor"</li>
 *     <li>Create {@link User} and attach to {@link Doctor}</li>
 *     <li>Persist via {@link AdminLinkService#createUserAndDoctor(User, Doctor)}</li>
 * </ol>
 *
 * @author MamenCortes
 * @author paulablancog
 */
public class NewDoctorPanel extends JPanel implements ActionListener {
    private Application appMain;
    private JLabel nameHeading;
    private MyTextField name;
    private JLabel surnameHeading;
    private MyTextField surname;
    private JLabel emailHeading;
    private MyTextField email;
    private JLabel passwordHeading;
    private MyTextField password;
    private JLabel phoneHeading;
    private MyTextField phoneNumber;
    private JLabel specHeading;
    private MyTextField speciality;
    private MyComboBox<String> nextStep;
    private JLabel departmentHeading;
    private MyTextField department;

    private JLabel title;
    protected String titleText = " ";
    protected JButton cancelButton;
    protected JLabel errorMessage;
    protected JPanel formContainer;
    private JButton saveChangesBt;
    private Boolean saved;


    //Format variables: Color and Font
    private final Color titleColor = Application.dark_purple;
    private final Font titleFont = new Font("sansserif", Font.BOLD, 25);
    private final Font contentFont = new Font("sansserif", 1, 12);
    private final Color contentColor = Application.dark_turquoise;


    /**
     * Creates the panel and initializes the form with placeholder values.
     *
     * @param appMain main application controller used for navigation and manager access
     */
    public NewDoctorPanel(Application appMain) {
        this.appMain = appMain;
        saved = false;
        initDoctorInfo();

    }

    /**
     * Defines initial text field setup with default hints and enables editing.
     * Delegates layout construction to {@link #initDoctorForm()}.
     */
    public void initDoctorInfo() {
        this.titleText = "Physician information";
        name = new MyTextField();
        name.setHint("Antonio");
        surname = new MyTextField();
        surname.setHint("Recio");
        name.setEnabled(true);
        email = new MyTextField();
        email.setHint("antonio.recio@nightguardian.com");
        email.setEnabled(true);
        password = new MyTextField();
        phoneNumber = new MyTextField();
        phoneNumber.setHint("123456789");
        phoneNumber.setEnabled(true);
        speciality = new MyTextField();
        speciality.setHint("Neurologist | Epilepsy Specialist ");
        speciality.setEnabled(true);
        department = new MyTextField();
        department.setHint(
                "Hospital General Universitario Gregorio Marañón");
        department.setEnabled(true);
        formContainer = new JPanel();
        initDoctorForm();
    }

    /**
     * Builds the full doctor registration form UI using MigLayout.
     * <p>
     * Includes:
     * <ul>
     *     <li>Personal data fields</li>
     *     <li>Professional data fields (speciality, department)</li>
     *     <li>Account fields (email, password)</li>
     *     <li>Error message label</li>
     *     <li>Save and cancel buttons</li>
     * </ul>
     * </p>
     */
    private void initDoctorForm() {
        this.setLayout(new MigLayout("fill", "[25%][25%][25%][25%]", "[][][][][][][][][][]"));
        this.setBackground(Color.white);
        formContainer.setBackground(Color.white);
        formContainer.setLayout(new MigLayout("fill, inset 10, gap 5, wrap 2", "[50%][50%]", "[][][][][][][][]push"));

        //Add Title
        title = new JLabel(titleText);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setForeground(titleColor);
        title.setFont(titleFont);
        title.setAlignmentY(LEFT_ALIGNMENT);
        title.setIcon(new ImageIcon(getClass().getResource("/icons/doctor-info64_2.png")));
        add(title, "cell 0 0 4 1, alignx left");
        add(formContainer,  "cell 0 1 4 7, grow, gap 10 10");

        //ROW 1
        //Name and surname
        nameHeading = new JLabel("Name*");
        nameHeading.setFont(contentFont);
        nameHeading.setForeground(contentColor);
        surnameHeading = new JLabel("Surname*");
        surnameHeading.setFont(contentFont);
        surnameHeading.setForeground(contentColor);
        formContainer.add(nameHeading, "grow");
        formContainer.add(surnameHeading, "grow");

        formContainer.add(name, "grow");
        formContainer.add(surname, "grow");

        //ROW 2
        specHeading = new JLabel("Speciality*");
        specHeading.setFont(contentFont);
        specHeading.setForeground(contentColor);
        formContainer.add(specHeading, "grow");
        departmentHeading = new JLabel("Department*");
        departmentHeading.setFont(contentFont);
        departmentHeading.setForeground(contentColor);
        formContainer.add(departmentHeading, "grow");

        formContainer.add(speciality, "grow");
        formContainer.add(department, "grow");

        //ROW 3
        phoneHeading = new JLabel("Phone Number*");
        phoneHeading.setFont(contentFont);
        phoneHeading.setForeground(contentColor);
        formContainer.add(phoneHeading, "grow");
        emailHeading = new JLabel("Email*");
        emailHeading.setFont(contentFont);
        emailHeading.setForeground(contentColor);
        formContainer.add(emailHeading, "grow");
        formContainer.add(phoneNumber, "grow");
        formContainer.add(email, "grow");

        //ROW 4
        passwordHeading = new JLabel("Password*");
        passwordHeading.setFont(contentFont);
        passwordHeading.setForeground(contentColor);
        formContainer.add(passwordHeading, "grow");
        formContainer.add(password, "grow, skip 1");

        //Add buttons
        cancelButton = new MyButton("CANCEL", Application.turquoise, Color.white);
        cancelButton.addActionListener(this);
        add(cancelButton, "cell 1 9, grow, center");

        saveChangesBt = new MyButton("SAVE AND GO BACK", Application.turquoise, Color.white);
        saveChangesBt.addActionListener(this);
        add(saveChangesBt, "cell 2 9, grow, center");

        errorMessage = new JLabel();
        errorMessage.setFont(new Font("sansserif", Font.BOLD, 12));
        errorMessage.setForeground(Color.red);
        errorMessage.setText("Error message test");
        this.add(errorMessage, "cell 0 8, span, center");
        errorMessage.setVisible(false);

    }

    /**
     * Creates a new {@link Doctor} and associated {@link User} based on form input.
     * <p>
     * The method:
     * <ul>
     *     <li>Reads form fields and populates a Doctor object</li>
     *     <li>Performs validations (phone, email, required fields, password)</li>
     *     <li>Retrieves the "Doctor" role from the Security DB</li>
     *     <li>Creates the User associated with this doctor</li>
     *     <li>Calls admin services to persist both objects</li>
     *     <li>If successful, resets the view and returns to {@link MainMenu}</li>
     * </ul>
     *
     * If validation fails or backend operations fail, an error message is shown.
     */
    private void createDoctorAndUSer() {
        Doctor d = new Doctor();
        try {
            d.setName(name.getText());
            d.setSurname(surname.getText());
            d.setEmail(email.getText());
            d.setContact(phoneNumber.getText());
            d.setSpeciality(speciality.getText());
            d.setDepartment(department.getText());

            Integer phonenumber;

            //Validate Phone number
            try {
                if (!phoneNumber.getText().isEmpty() && phoneNumber.getText().length() == 9){
                    phonenumber = Integer.parseInt(phoneNumber.getText());
                    d.setContact(phonenumber.toString());
                }else {
                    showErrorMessage("Please, set a phone number of 9 digits");
                    return;
                }
            } catch (NumberFormatException exFecha) {
                showErrorMessage("The phone number must be a number");
                return;
            }


            if(!validateEmail(d.getEmail())){return;} //validateEmail already show an error message
            if(!validatePassword(password.getText())){return;} //validatePassword already shows an error message
            Role role = appMain.securityManager.getRoleJDBC().findRoleByName("Doctor");
            User u = new User(d.getEmail(), password.getText(), role.getId(), true);

            if (d.getName().isEmpty() || d.getSurname().isEmpty() || d.getContact().isEmpty() || speciality.getText().isEmpty() || department.getText().isEmpty()) {
                showErrorMessage("Please fill all the fields");
            }else {
                //TODO: Cambiar para register
                if(!appMain.adminLinkService.createUserAndDoctor(u, d)){
                    showErrorMessage("Error creating user and doctor");
                    saved = false;
                    return;
                }
                saved = true;
                resetView();
                appMain.changeToMainMenu();
            }

        } catch (Exception ex) {
            showErrorMessage("Error creating user and doctor");
        }
    }

    /**
     * Handles interactions with the Save and Cancel buttons.
     * <ul>
     *     <li><b>Cancel</b> → if unsaved, shows confirmation dialog; otherwise returns to main menu</li>
     *     <li><b>Save</b> → triggers {@link #createDoctorAndUSer()}</li>
     * </ul>
     *
     * @param e action event triggered by button clicks
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
            createDoctorAndUSer();
        }
    }

    /**
     * Clears all fields and returns the panel to its initial state.
     * Called after canceling or after a successful save.
     */
    private void resetView(){
        name.setText("");
        surname.setText("");
        email.setText("");
        phoneNumber.setText("");
        password.setText("");
        errorMessage.setVisible(false);
        speciality.setText("");
        department.setText("");
        saved = false;
    }

    /**
     * Displays a confirmation dialog asking if the user wants to leave without saving.
     * If YES, resets the form and goes back to the main menu.
     *
     * @param parentFrame parent window for dialog centering
     * @param question    text shown in the confirmation dialog
     */
    private void showQuestionPanel(JFrame parentFrame, String question) {
        MyButton okButton = new MyButton("YES");
        MyButton cancelButton = new MyButton("CONTINUE EDITING");

        QuestionDialog panel = new QuestionDialog(question, okButton, cancelButton);
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
     * Requirements:
     * <ul>
     *     <li>Minimum 8 characters</li>
     *     <li>At least one numerical digit</li>
     * </ul>
     *
     * @param password password entered in the form
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
     * Validates that the email belongs to the institutional domain
     * <code>@nightguardian.com</code>.
     *
     * @param email email string to validate
     * @return true if valid and institutional, false otherwise
     */
    private Boolean validateEmail(String email) {
        if(!email.isBlank() && email.contains("@")) {
            String[] emailSplit = email.split("@");
            if(emailSplit.length >1 && emailSplit[1].equals("nightguardian.com")){
                return true;
            }
        }
        showErrorMessage("Invalid Email");
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
}