package ui.windows;

import net.bytebuddy.asm.Advice;
import org.example.entities_medicaldb.Patient;
import net.miginfocom.swing.MigLayout;
import ui.components.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;


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

    //private JDateChooser birthDate;
    public NewPatientPanel(Application appMain) {
        this.appMain = appMain;
        saved = false;
        initPatientInfo();

    }

    public void initPatientInfo() {
        this.titleText = "Patient information";

        //Initialize values
        name = new MyTextField();
        //name.setText("Jane");
        name.setEnabled(true); //Doesnt allow editing
        surname = new MyTextField();
        //surname.setText("Doe");
        surname.setEnabled(true);
        email = new MyTextField();
        //email.setText("jane.doe@gmail.com");
        email.setEnabled(true);
        phoneNumber = new MyTextField();
        //phoneNumber.setText("123456789");
        phoneNumber.setEnabled(true);
        //sex = new MyTextField();
        gender = new MyComboBox<String>();
        gender.addItem("Male");
        gender.addItem("Female");
        gender.addItem("Non-binary");
        //sex.setText("Non Binary");
        birthDate = new MyTextField();
        birthDate.setHint("YYYY-MM-DD");
        //birthDate.setText("1999-11-11");
        birthDate.setEnabled(true);
        password = new MyTextField();
        formContainer = new JPanel();
        initPatientForm();
    }

    private void initPatientForm() {
        //this.setLayout(new MigLayout("fill, inset 15, gap 0, wrap 4, debug", "[][][][]", "[][][][][][][][][][]"));
        this.setLayout(new MigLayout("fill", "[][][][]", "[][][][][][][][][]push[]"));
        this.setBackground(Color.white);
        //this.setBackground(Application.lighter_turquoise);
        formContainer.setBackground(Color.white);
        formContainer.setLayout(new MigLayout("fill, inset 10, gap 5, wrap 2", "[grow 50][grow 50]", "[][][][][][][]push"));

        //Add Title
        title = new JLabel(titleText);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setForeground(titleColor);
        title.setFont(titleFont);
        title.setAlignmentY(LEFT_ALIGNMENT);
        title.setIcon(new ImageIcon(getClass().getResource("/icons/patient-info64-2.png")));
        add(title, "cell 0 0 4 1, alignx left");

        //add(formContainer,  "cell 0 1 4 8, grow, gap 10");
        //place in column 0, row 1, expand to 4 columns and 8 rows. Gap 10px left and right
        add(formContainer,  "cell 0 1 4 7, grow, gap 10 10");

        //add(title1, "cell 0 0, grow");

        //ROW 1
        //Name and surname
        nameHeading = new JLabel("Name*");
        nameHeading.setFont(contentFont);
        nameHeading.setForeground(contentColor);
        formContainer.add(nameHeading, "cell 0 0");
        //add(nameText, "skip 1, grow");

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

        passwordHeading = new JLabel("Password*");
        passwordHeading.setFont(contentFont);
        passwordHeading.setForeground(contentColor);
        formContainer.add(passwordHeading, "grow");

        //ROW 4
        formContainer.add(gender, "grow");
        formContainer.add(birthDate,  "grow"); //TODO create birth date chooser

        //ROW 5
        emailHeading = new JLabel("Email*");
        emailHeading.setFont(contentFont);
        emailHeading.setForeground(contentColor);
        formContainer.add(emailHeading, "grow");

        phoneHeading = new JLabel("Phone Number*");
        phoneHeading.setFont(contentFont);
        phoneHeading.setForeground(contentColor);
        formContainer.add(phoneHeading, "grow");

        //ROW 5
        formContainer.add(email, "grow");
        formContainer.add(phoneNumber, "grow");

        //ROW 6
        formContainer.add(passwordHeading, "grow");
        formContainer.add(password, "grow, skip 1");

        //Add buttons
        cancelButton = new MyButton("CANCEL", Application.turquoise, Color.white);
        cancelButton.addActionListener(this);
        //add(goBackButton,"cell 1 7, left, gapx 10, gapy 5");
        add(cancelButton, "cell 1 9, growx, center");

        saveChangesBt = new MyButton("SAVE AND GO BACK", Application.turquoise, Color.white);
        saveChangesBt.addActionListener(this);
        add(saveChangesBt, "cell 2 9, growx, center");

        errorMessage = new JLabel();
        errorMessage.setFont(new Font("sansserif", Font.BOLD, 12));
        errorMessage.setForeground(Color.red);
        errorMessage.setText("Error message test");
        //this.add(errorMessage, "cell 0 8, span, left");
        this.add(errorMessage, "cell 0 8, span, center");
        errorMessage.setVisible(true);

    }

    //TODO: Check validations
    private void createPatientAndUser() {
        Patient p = new Patient();
        try {
            p.setName(name.getText());
            p.setSurname(surname.getText());
            p.setGender(gender.getSelectedItem().toString());
            p.setEmail(email.getText());
            p.setContact(phoneNumber.getText());
            LocalDate fecha;
            Integer phonenumber;

            //Validate Date of Birth
            try {
                if (!birthDate.getText().isEmpty()) {
                    fecha = LocalDate.parse(birthDate.getText());
                    p.setDateOfBirth(fecha);
                }
            } catch (DateTimeParseException exFecha) {
                errorMessage.setText("Invalid date (use YYYY-MM-DD)");
                errorMessage.setForeground(Color.RED);
                return;
            }

            //Validate Phone number
            try {
                if (!phoneNumber.getText().isEmpty() && phoneNumber.getText().length() == 9){
                    phonenumber = Integer.parseInt(phoneNumber.getText());
                    p.setContact(phonenumber.toString());
                }else {
                    errorMessage.setText("Please, set a phone number of 6 digits");
                    errorMessage.setForeground(Color.RED);
                    return;
                }
            } catch (NumberFormatException exFecha) {
                errorMessage.setText("The phone must be a numeric value");
                errorMessage.setForeground(Color.RED);
                return;
            }

            //TODO: validate email
            //TODO: validate password
            //TODO: create user

            if (p.getName().isEmpty() || p.getSurname().isEmpty()|| p.getGender().isEmpty() || p.getEmail().isEmpty() || p.getContact().isEmpty() || password.getText() == "") {
                errorMessage.setText("Please fill all the fields");
                errorMessage.setForeground(Color.RED);
                errorMessage.setVisible(true);
            } else {
                //TODO: incluir metodo update en el JDBC
                //PatientJDBC patientJDBC = new PatientJDBC();
                //patientJDBC.updatePatient(p);
                if(!appMain.patientJDBC.insertPatient(p)){
                    errorMessage.setText("Error creating patient");
                    errorMessage.setForeground(Color.RED);
                    errorMessage.setVisible(true);
                    saved = false;
                    return;
                }

                saved = true;
                errorMessage.setText("Profile created successfully");
                errorMessage.setForeground(new Color(0, 128, 0));
                errorMessage.setVisible(true);
                resetView();
                appMain.changeToMainMenu();
            }

        } catch (Exception ex) {
            errorMessage.setText("Error creating profile");
            errorMessage.setForeground(Color.RED);
            errorMessage.setVisible(true);
        }
    }

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

    private void showQuestionPanel(JFrame parentFrame, String question) {
        MyButton okButton = new MyButton("YES");
        MyButton cancelButton = new MyButton("CONTINUE EDITING");

        QuestionDialog panel = new QuestionDialog(question, okButton, cancelButton);
        JDialog dialog = new JDialog(parentFrame, "Change Password", true);
        dialog.getContentPane().add(panel);
        dialog.getContentPane().setBackground(Color.white);
        dialog.pack();
        dialog.setLocationRelativeTo(parentFrame);
        //dialog.setSize(400, 200);

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
}