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

    public MainMenu(Application appMain) {
        //super();
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

    private void showCheckConnectedClients(JFrame parentFrame) {
        /*ArrayList<String> clients = new ArrayList<>();
        clients.add("Client 1: socket1");
        clients.add("Client 2: socket2");
        clients.add("Client 3: socket3");*/
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
            //dialog.setSize(400, 200);

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