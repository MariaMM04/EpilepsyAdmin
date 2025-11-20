package ui.windows;

import Exceptions.*;
import net.miginfocom.swing.MigLayout;
import network.Server;
import org.example.JDBC.medicaldb.DoctorJDBC;
import org.example.JDBC.medicaldb.MedicalManager;
import org.example.JDBC.medicaldb.PatientJDBC;
import org.example.JDBC.securitydb.*;
import org.example.entities_securitydb.User;
import org.example.service.AdminLinkService;
import ui.components.MyButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

    //Network
    private int serverPort = 9009;
    public Server server;
    public User user;

    //Managers
    public PatientJDBC patientJDBC;
    public MedicalManager medicalManager;
    public AdminLinkService adminLinkService;
    public DoctorJDBC doctorJDBC;
    public org.example.JDBC.securitydb.SecurityManager securityManager;
    public UserJDBC userJDBC;

    public static void main(String[] args){
        //La aplicación se ejecuta en su propio hilo especial EDT (Event Dispatch Thread)
        SwingUtilities.invokeLater(() -> {
            Application app = new Application(); // inicializa tu GUI
            app.setVisible(true);
        });
    }

    public Application() {
        initComponents();
        //setBounds(100, 100, 602, 436);
        setBounds(100, 100, 650, 500);

        //Managers
        medicalManager = new MedicalManager();
        patientJDBC = medicalManager.getPatientJDBC();
        doctorJDBC = medicalManager.getDoctorJDBC();
        securityManager = new org.example.JDBC.securitydb.SecurityManager();
        userJDBC = securityManager.getUserJDBC();
        adminLinkService = new AdminLinkService(medicalManager, securityManager);

        //Network
        server = new Server(serverPort, this);
        server.startServer();

        //Panels
        appPanels = new ArrayList<JPanel>();
        logInPanel = new UserLogIn(this);
        mainMenu = new MainMenu(this);
        appPanels.add(logInPanel);
        appPanels.add(mainMenu);

        this.setContentPane(mainMenu);
    }

    public void initComponents() {
        setTitle("Application");
        //setSize(602, 436);
        setLayout(null);
        setLocationRelativeTo(null);
        setIconImage(new ImageIcon(getClass().getResource("/icons/night_guardian_mini_500.png")).getImage());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);


        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                System.out.println("Window is closing...");

                if (server.isRunning()) {
                    if(server.getConnectedClients().isEmpty()){
                        try {
                            server.stop();
                            System.exit(0);
                        } catch (ClientError ex) {
                            showMessageDialog(null, ex.getMessage());
                        }
                    }else{
                        int option = JOptionPane.showConfirmDialog(
                                null,
                                "There are clients still connected\nDo you want to interrupt the connection?",
                                "Error",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.ERROR_MESSAGE
                        );
                        if(option == JOptionPane.YES_OPTION){
                            try {
                                server.stop();
                                System.exit(0);
                            } catch (ClientError exc) {
                                showMessageDialog(null, exc.getMessage());
                            }
                        }
                    }
                }else{
                    System.out.println("Server is already stopped");
                    System.exit(0);
                }
            }
        });

    }

    private void askConfirmationCloseConnectedClients(){
        int option = JOptionPane.showConfirmDialog(
                null,
                "There are clients still connected\nDo you want to interrupt the connection?",
                "Error",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE
        );
        if(option == JOptionPane.YES_OPTION){
            try {
                server.stop();
                System.exit(0);
            } catch (ClientError exc) {
                showMessageDialog(null, exc.getMessage());
            }
        }
    }

    public void changeToUserLogIn() {
        hideAllPanels();
        logInPanel.setVisible(true);
        this.setContentPane(logInPanel);
    }

    public void changeToPanel(JPanel panel) {
        hideAllPanels();
        appPanels.add(panel);
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

    public static void showMessageDialog(JFrame parentFrame, String message) {
        JPanel panel = new JPanel();
        panel.setLayout(new MigLayout("wrap, fill, inset 15", "[center]", "push[]25[]push"));
        panel.setBackground(Color.white);
        panel.setPreferredSize(new Dimension(400, 300));

        JLabel label = new JLabel(message);
        label.setFont(new Font("sansserif", 1, 25));
        label.setForeground(Application.dark_purple);
        //panel.add(label, "center, wrap");

        JTextArea labelLikeText = new JTextArea(message);
        labelLikeText.setLineWrap(true);
        labelLikeText.setWrapStyleWord(true);
        labelLikeText.setEditable(false);
        labelLikeText.setOpaque(false); // looks like a JLabel
        labelLikeText.setFont(new Font("sansserif", 1, 20));
        labelLikeText.setBackground(Color.white);
        labelLikeText.setForeground(Application.dark_purple);
        panel.add(labelLikeText, "growx, center, wrap");

        MyButton okButton = new MyButton("OK", Application.turquoise, Color.white);
        panel.add(okButton, "center");

        JDialog dialog = new JDialog(parentFrame, "Message dialog", false); //dont allow interacting with other panels at the same time
        dialog.getContentPane().add(panel);
        dialog.getContentPane().setBackground(Color.white);
        dialog.pack();
        dialog.setLocationRelativeTo(parentFrame);
        //dialog.setSize(400, 200);

        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {dialog.dispose();}
        });

        dialog.setVisible(true);
    }

}
