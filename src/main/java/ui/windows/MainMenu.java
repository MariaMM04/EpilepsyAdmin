package ui.windows;

import ui.components.MenuTemplate;
import ui.components.MyButton;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class MainMenu extends MenuTemplate {
    private static final long serialVersionUID = 6050014345831062858L;
    private ImageIcon logoIcon;
    private JButton createPatientBt;
    private JButton createDoctorBt;
    private JButton seePatientListBt;
    private JButton seeDoctorListBt;
    private JButton verifyConnectedClientsBt;
    private JButton stopServerBt;
    private JButton logOutBt;
    private Application appMenu;
    private String company_name;

    //Panels
    private NewPatientPanel newPatientPanel;
    private NewDoctorPanel newDoctorPanel;

    public MainMenu(Application appMenu) {
        //super();
        this.appMenu = appMenu;
        //Initialize panels
        newPatientPanel = new NewPatientPanel(appMenu);
        newDoctorPanel = new NewDoctorPanel(appMenu);

        addButtons();
        company_name = "NIGHT GUARDIAN: EPILEPSY";
        //company_name = "<html>NIGHT GUARDIAN<br>EPILEPSY</html>";
        //company_name ="<html><div style='text-align: center;'>NIGHT GUARDIAN<br>EPILEPSY</div></html>";

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
        stopServerBt = new MyButton("Stop Server");
        logOutBt = new MyButton("Log Out");

        buttons.add(createPatientBt);
        buttons.add(createDoctorBt);
        buttons.add(seeDoctorListBt);
        buttons.add(seePatientListBt);
        buttons.add(verifyConnectedClientsBt);
        buttons.add(stopServerBt);
        buttons.add(logOutBt);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()== seeDoctorListBt) {
            //appMenu.changeToPanel();
        }else if(e.getSource()== seePatientListBt) {
            //appMenu.changeToPanel();
        }else if(e.getSource()== logOutBt) {
            appMenu.changeToUserLogIn();
        }else if(e.getSource()== createPatientBt) {

            appMenu.changeToPanel(newPatientPanel);
            //ventanaPaciente.setVisible(true);

        }else if(e.getSource()== createDoctorBt) {
            appMenu.changeToPanel(newDoctorPanel);
            //DoctorProfileWindow ventanaDoctor = new DoctorProfileWindow();
            //ventanaDoctor.setVisible(true);
        }else if(e.getSource()== verifyConnectedClientsBt) {

        }else if(e.getSource()== stopServerBt) {

        }

    }
}