package ui.windows;

import org.example.JDBC.medicaldb.DoctorJDBC;
import org.example.JDBC.medicaldb.MedicalConnection;
import org.example.JDBC.medicaldb.MedicalManager;
import org.example.JDBC.medicaldb.PatientJDBC;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class Application extends JFrame {
    public static Color darker_purple = new Color(114, 82, 153); //#725299
    public static Color dark_purple = new Color(170, 84, 204); //#AA54CC
    public static Color pink = new Color(226, 169, 241); //#E2A9F1
    public static Color purple = new Color(196, 158, 207);
    public static Color turquoise = new Color(94, 186, 176); //#5EBAB0
    public static Color light_purple = new Color(239, 232, 255); //#EFE8FF
    public static Color light_turquoise = new Color(193, 252, 244); //#C1FCF4
    //public static Color light_turquoise = new Color(213, 242, 236); //#d5f2ec
    public static Color lighter_turquoise = new Color(243, 250, 249);//#f3faf9
    public static Color darker_turquoise = new Color(73, 129, 122);
    public static Color dark_turquoise = new Color(52, 152, 143); //#34988f

    //Panels
    private ArrayList<JPanel> appPanels;
    private UserLogIn logInPanel;
    private MainMenu mainMenu;


    //Managers
    public PatientJDBC patientJDBC;
    public MedicalManager medicalManager;
    public DoctorJDBC doctorJDBC;

    public static void main(String[] args) {
        Application app = new Application();
        app.setVisible(true);
    }

    public Application() {
        initComponents();
        setBounds(100, 100, 602, 436);

        //Panels
        appPanels = new ArrayList<JPanel>();
        logInPanel = new UserLogIn(this);
        mainMenu = new MainMenu(this);
        appPanels.add(logInPanel);
        appPanels.add(mainMenu);

        //Managers
        medicalManager = new MedicalManager();
        patientJDBC = medicalManager.getPatientJDBC();
        doctorJDBC = medicalManager.getDoctorJDBC();

        this.setContentPane(mainMenu);
    }

    public void initComponents() {
        setTitle("Application");
        //setSize(602, 436);
        setLayout(null);
        setLocationRelativeTo(null);
        setIconImage(new ImageIcon(getClass().getResource("/icons/night_guardian_mini_500.png")).getImage());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    public void changeToUserLogIn() {
        hideAllPanels();
        logInPanel.setVisible(true);
        this.setContentPane(logInPanel);
    }

    public void changeToPanel(JPanel panel) {
        hideAllPanels();
        panel.setVisible(true);
        this.setContentPane(panel);
    }

    public void changeToMainMenu(){
        hideAllPanels();
        if (mainMenu == null) {
            mainMenu = new MainMenu(this);
            appPanels.add(mainMenu);
            System.out.println("Admin initialized");
        }

        mainMenu.setVisible(true);
        this.setContentPane(mainMenu);

    }

    private void hideAllPanels() {
        for (JPanel jPanel : appPanels) {
            if(jPanel.isVisible()) {
                jPanel.setVisible(false);
            }
        }
    }
}
