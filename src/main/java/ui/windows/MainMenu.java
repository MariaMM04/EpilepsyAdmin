package ui.windows;

import Exceptions.*;
import network.Server;
import org.example.entities_medicaldb.Doctor;
import org.example.entities_medicaldb.Patient;
import ui.components.*;

import javax.swing.*;
import java.util.List;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
/**
 * Main administrative dashboard of the Night Guardian system.
 * <p>
 * This panel is displayed after a successful administrator login and serves
 * as the central navigation hub for all administrative operations. It provides
 * access to:
 * </p>
 *
 * <ul>
 *     <li>Patient management (create & list)</li>
 *     <li>Doctor management (create & list)</li>
 *     <li>Verification of active server connections</li>
 *     <li>Restarting the internal server</li>
 *     <li>Logging out the current user</li>
 * </ul>
 *
 * <h2>Lifecycle</h2>
 * <ol>
 *     <li>The constructor receives the {@link Application} instance (controller).</li>
 *     <li>All secondary panels (new patient, new doctor, search views) are created immediately.</li>
 *     <li>The {@link #init(ImageIcon, String)} method from {@link MenuTemplate} builds the UI:
 *         header, logo, company name, and button layout.</li>
 *     <li>Button clicks trigger navigation using {@link Application#changeToPanel(JPanel)}.</li>
 *     <li>This panel persists across the entire application lifetime; it is not recreated.</li>
 * </ol>
 *
 * <h2>Navigation Diagram</h2>
 * <pre>
 * ┌──────────────────┐
 * │     MainMenu     │
 * └───▲──────┬──────┘
 *     │      │
 *     │      ├──► SearchPatient
 *     │      ├──► SearchDoctor
 *     │      ├──► NewPatientPanel
 *     │      ├──► NewDoctorPanel
 *     │      ├──► CheckConnectedClients (modal)
 *     │      └──► Restart Server
 * </pre>
 *
 * @author MamenCortes
 */
public class MainMenu extends MenuTemplate {
    private static final long serialVersionUID = 6050014345831062858L;
    private ImageIcon logoIcon;
    private JButton createPatientBt;
    private JButton createDoctorBt;
    private JButton seePatientListBt;
    private JButton seeDoctorListBt;
    private JButton verifyConnectedClientsBt;
    private JButton restartServerBt;
    private JButton logOutBt;
    private Application appMain;
    private String company_name;

    //Panels
    private NewPatientPanel newPatientPanel;
    private NewDoctorPanel newDoctorPanel;
    private SearchPatient searchPatientPanel;
    private SearchDoctor searchDoctorPanel;
    /**
     * Creates the MainMenu and all its subviews.
     * <p>
     * Child panels include:
     * <ul>
     *     <li>{@link NewPatientPanel} – create a new patient</li>
     *     <li>{@link NewDoctorPanel} – create a new doctor</li>
     *     <li>{@link SearchPatient} – view/search patient list</li>
     *     <li>{@link SearchDoctor} – view/search doctor list</li>
     * </ul>
     * </p>
     *
     * @param appMain reference to the global {@link Application} controller
     */
    public MainMenu(Application appMain) {
        this.appMain = appMain;
        //Initialize panels
        newPatientPanel = new NewPatientPanel(appMain);
        newDoctorPanel = new NewDoctorPanel(appMain);
        searchPatientPanel = new SearchPatient(appMain);
        searchDoctorPanel = new SearchDoctor(appMain);

        addButtons();
        company_name = "NIGHT GUARDIAN: EPILEPSY";
        logoIcon = new ImageIcon(getClass().getResource("/icons/night_guardian_mini_128.png"));
        this.init(logoIcon, company_name);
    }

    /**
     * Adds all the menu buttons to the button list inherited from {@link MenuTemplate}.
     * <p>
     * These buttons define the main admin actions and must be added before calling {@link #init}.</p>
     */
    private void addButtons() {
        //Default color: light purple
        seePatientListBt = new MyButton("Patients List");
        seeDoctorListBt = new MyButton("Doctors List");
        createPatientBt = new MyButton("Create Patient");
        createDoctorBt = new MyButton("Create Doctor");
        verifyConnectedClientsBt = new MyButton("Verify Connected Clients");
        restartServerBt = new MyButton("Restart Server");
        logOutBt = new MyButton("Log Out");

        buttons.add(createPatientBt);
        buttons.add(createDoctorBt);
        buttons.add(seeDoctorListBt);
        buttons.add(seePatientListBt);
        buttons.add(verifyConnectedClientsBt);
        buttons.add(restartServerBt);
        buttons.add(logOutBt);
    }

    /**
     * Handles button actions defined in this menu.
     *
     * <h3>Actions</h3>
     * <ul>
     *     <li><b>Patients List:</b> Retrieves all patients (with assigned doctors) and opens the search panel.</li>
     *     <li><b>Doctors List:</b> Retrieves all doctors and opens the doctor list panel.</li>
     *     <li><b>Create Patient:</b> Opens the patient creation form.</li>
     *     <li><b>Create Doctor:</b> Opens the doctor creation form.</li>
     *     <li><b>Verify Connected Clients:</b> Opens a modal with the list of connected clients.</li>
     *     <li><b>Restart Server:</b> Restarts the admin server if it is not currently running.</li>
     *     <li><b>Log Out:</b> Returns to the login panel.</li>
     * </ul>
     *
     * @param e action event triggered by user interaction
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()== seePatientListBt) {
            System.out.println("Open search patient view");
            List<Patient> patients = appMain.adminLinkService.getAllPatientsWithDoctor();
            searchPatientPanel.updatePatientDefModel(patients);
            appMain.changeToPanel(searchPatientPanel);
        }else if(e.getSource()== seeDoctorListBt) {
            System.out.println("Open search doctor view");
            List<Doctor> doctors = appMain.doctorJDBC.getAllDoctors();
            searchDoctorPanel.updateDoctorDefModel(doctors);
            appMain.changeToPanel(searchDoctorPanel);
        }else if(e.getSource()== logOutBt) {
            appMain.changeToUserLogIn();
        }else if(e.getSource()== createPatientBt) {
            System.out.println("Open new patient view");
            newPatientPanel.updateView();
            appMain.changeToPanel(newPatientPanel);
        }else if(e.getSource()== createDoctorBt) {
            appMain.changeToPanel(newDoctorPanel);
        }else if(e.getSource()== verifyConnectedClientsBt) {
            showCheckConnectedClients(appMain);
        }else if(e.getSource()== restartServerBt) {
            if(appMain.server.isRunning()){
                Application.showMessageDialog(appMain, "The server is already running");
            }else{
                appMain.server.startServer();
                Application.showMessageDialog(appMain, "Server Started");
            }
        }

    }
    /**
     * Displays a modal dialog listing all currently connected clients.
     * Provides controls to refresh or stop the server safely.
     *
     * @param parentFrame the application frame used to center the dialog
     */
    private void showCheckConnectedClients(JFrame parentFrame) {
        ArrayList<String> clients;
        if(appMain.server.isRunning()){
            clients = appMain.server.getConnectedClients();
            MyButton goBackBt = new MyButton();
            MyButton stopServerBt = new MyButton();

            CheckConnectedClients panel = new CheckConnectedClients(clients, goBackBt, stopServerBt );
            panel.setBackground(Color.white);
            panel.setPreferredSize(new Dimension(400, 300));

            JDialog dialog = new JDialog(parentFrame, "Check connected clients", false); //dont allow interacting with other panels at the same time
            dialog.getContentPane().add(panel);
            dialog.getContentPane().setBackground(Color.white);
            dialog.pack();
            dialog.setLocationRelativeTo(parentFrame);

            goBackBt.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {dialog.dispose();}
            });

            stopServerBt.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(!appMain.server.isRunning()){
                        panel.showErrorMessage("Server is not running");
                    }
                    else if(clients.size()>0) {
                        panel.showErrorMessage("Close all the connections before stopping the server");
                    }else{
                        try {
                            //TODO: ask for correct password
                            String password = JOptionPane.showInputDialog(parentFrame, "Enter the password:");
                            if(password.equals("1234")){
                                appMain.server.stop();
                                panel.showErrorMessage("Server stopped");
                            }else{
                                panel.showErrorMessage("Incorrect password");
                            }
                        } catch (ClientError ex) {
                            panel.showErrorMessage(ex.getMessage());
                            System.out.println(ex.getMessage());
                        }
                    }
                }
            });

            dialog.setVisible(true);
        }
    }

}