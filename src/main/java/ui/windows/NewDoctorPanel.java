package ui.windows;

import net.miginfocom.swing.MigLayout;
import org.example.entities_medicaldb.Doctor;
import org.example.entities_medicaldb.Patient;
import ui.components.MyButton;
import ui.components.MyComboBox;
import ui.components.MyTextField;
import ui.components.QuestionDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

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
    private JLabel officeHeading;
    private MyTextField office;

    private JLabel title;
    protected String titleText = " ";
    //protected JButton applyChanges;
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


    //private JDateChooser birthDate;
    public NewDoctorPanel(Application appMain) {
        this.appMain = appMain;
        saved = false;
        initDoctorInfo();

    }

    public void initDoctorInfo() {
        this.titleText = "Physician information";

        //Initialize values
        //TODO: replace with actual doctor values
        name = new MyTextField();
        name.setHint("Michal");
        surname = new MyTextField();
        surname.setHint("Alhajjar");
        name.setEnabled(true);
        email = new MyTextField();
        email.setHint("michal.alhajjar@gmail.com");
        email.setEnabled(true);
        password = new MyTextField();
        phoneNumber = new MyTextField();
        phoneNumber.setHint("123456789");
        phoneNumber.setEnabled(true);
        speciality = new MyTextField();
        speciality.setHint("Neurologist | Epilepsy Specialist ");
        speciality.setEnabled(true);
        office = new MyTextField();
        office.setHint(
                "Hospital General Universitario Gregorio Marañón");
        office.setEnabled(true);
        formContainer = new JPanel();
        initDoctorForm();
    }

    private void initDoctorForm() {
        //this.setLayout(new MigLayout("fill, inset 15, gap 0, wrap 4, debug", "[][][][]", "[][][][][][][][][][]"));
        this.setLayout(new MigLayout("fill, debug", "[25%][25%][25%][25%]", "[][][][][][][][][][]"));
        this.setBackground(Color.white);
        //this.setBackground(Application.light_purple);
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

        //add(formContainer,  "cell 0 1 4 8, grow, gap 10");
        //place in column 0, row 1, expand to 4 columns and 8 rows. Gap 10px left and right
        add(formContainer,  "cell 0 1 4 7, grow, gap 10 10");

        //add(title1, "cell 0 0, grow");

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

        //ROW 5
        specHeading = new JLabel("Speciality*");
        specHeading.setFont(contentFont);
        specHeading.setForeground(contentColor);
        formContainer.add(specHeading, "grow");
        officeHeading = new JLabel("Office/Department*");
        officeHeading.setFont(contentFont);
        officeHeading.setForeground(contentColor);
        formContainer.add(officeHeading, "grow");

        formContainer.add(speciality, "grow");
        formContainer.add(office, "grow");

        //ROW 3
        phoneHeading = new JLabel("Phone Number*");
        phoneHeading.setFont(contentFont);
        phoneHeading.setForeground(contentColor);
        formContainer.add(phoneHeading, "grow");
        emailHeading = new JLabel("Email*");
        emailHeading.setFont(contentFont);
        emailHeading.setForeground(contentColor);
        formContainer.add(emailHeading, "grow");

        //ROW 4
        formContainer.add(phoneNumber, "grow");
        formContainer.add(email, "grow");

        //ROW 3
        passwordHeading = new JLabel("Password*");
        passwordHeading.setFont(contentFont);
        passwordHeading.setForeground(contentColor);
        formContainer.add(passwordHeading, "grow");
        //add(nameText, "skip 1, grow");

        //ROW 4
        formContainer.add(password, "grow, skip 1");

        //Add buttons
        cancelButton = new MyButton("CANVEL", Application.turquoise, Color.white);
        cancelButton.addActionListener(this);
        //add(goBackButton,"cell 1 7, left, gapx 10, gapy 5");
        add(cancelButton, "cell 1 9, grow, center");

        saveChangesBt = new MyButton("SAVE", Application.turquoise, Color.white);
        saveChangesBt.addActionListener(this);
        add(saveChangesBt, "cell 2 9, grow, center");

        errorMessage = new JLabel();
        errorMessage.setFont(new Font("sansserif", Font.BOLD, 12));
        errorMessage.setForeground(Color.red);
        errorMessage.setText("Error message test");
        //this.add(errorMessage, "cell 0 8, span, left");
        this.add(errorMessage, "cell 0 8, span, center");
        errorMessage.setVisible(true);

    }

    private void createDoctorAndUSer() {
        Doctor d = new Doctor();
        try {
            d.setName(name.getText());
            d.setSurname(surname.getText());
            d.setEmail(email.getText());
            d.setContact(phoneNumber.getText());
            d.setSpeciality(speciality.getText());
            d.setDepartment(office.getText());

            Integer phonenumber;

            //Validate Phone number
            try {
                if (!phoneNumber.getText().isEmpty() && phoneNumber.getText().length() == 9){
                    phonenumber = Integer.parseInt(phoneNumber.getText());
                    d.setContact(phonenumber.toString());
                }else {
                    errorMessage.setText("Please, set a phone number of 9 digits");
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

            if (d.getName().isEmpty() || d.getSurname().isEmpty() || d.getEmail().isEmpty() || d.getContact().isEmpty() || speciality.getText().isEmpty() || office.getText().isEmpty() || password.getText() == "") {
                errorMessage.setText("Please fill all the fields");
                errorMessage.setForeground(Color.RED);
                errorMessage.setVisible(true);
            } else {
                if(!appMain.doctorJDBC.insertDoctor(d)){
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
            createDoctorAndUSer();
        }
    }


    private void resetView(){
        name.setText("");
        surname.setText("");
        email.setText("");
        phoneNumber.setText("");
        password.setText("");
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