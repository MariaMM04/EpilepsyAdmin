package ui.windows;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import net.miginfocom.swing.MigLayout;
import org.example.entities_medicaldb.Doctor;
import ui.RandomData;
import ui.components.DoctorCell;
import ui.components.MyButton;
import ui.components.MyTextField;

import javax.swing.*;

public class SearchDoctor extends JPanel implements ActionListener, MouseListener {

    private Application appMain;
    protected final Font titleFont = new Font("sansserif", 3, 15);
    protected final Color titleColor = Application.dark_purple;
    protected JLabel title;
    protected String titleText = " Search Doctors ";
    protected ImageIcon icon  = new ImageIcon(getClass().getResource("/icons/patient-info64-2.png"));
    protected JScrollPane scrollPane1;
    protected String searchText = "Search By Surname";
    protected MyTextField searchByTextField;
    protected MyButton searchButton;
    protected MyButton resetListButton;
    //protected MyButton openFormButton;
    protected JLabel errorMessage;
    protected MyButton goBackButton;
    //protected Application appMain;
    protected JList<Doctor> doctorsList;
    protected DefaultListModel<Doctor> doctorsDefListModel;
    private List<Doctor> allDoctors;

    public SearchDoctor(Application appMain) {
        this.appMain = appMain;
        initMainPanel();
        List<Doctor> doctors = new ArrayList<>();
        for(int i = 0; i < 10; i++) {
            doctors.add(RandomData.generateRandomDoctor());
        }
        //showDoctors(doctors);
        System.out.println("Search Doctors Panel Successfully created");
        //showPatients(null);
    }

    private void initMainPanel() {
        this.setLayout(new MigLayout("fill, inset 20, gap 0, wrap 3", "[grow 5]5[grow 5]5[grow 40][grow 40]", "[][][][][][][][][][]"));
        this.setBackground(Color.white);
        //Add Title
        title = new JLabel(titleText);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setForeground(titleColor);
        title.setFont(new Font("sansserif", Font.BOLD, 25));
        title.setAlignmentY(LEFT_ALIGNMENT);
        title.setIcon(icon);
        add(title, "cell 0 0 3 1, alignx left");

        //Initialize search panel
        JLabel searchTitle = new JLabel(searchText);
        searchTitle.setFont(titleFont);
        searchTitle.setForeground(Application.darker_purple);
        add(searchTitle, "cell 0 1 2 1, alignx center, grow");

        searchByTextField = new MyTextField("ex. Doe...");
        searchByTextField.setBackground(Application.lighter_turquoise);
        searchByTextField.setHint("ex. Doe");
        add(searchByTextField, "cell 0 2 2 1, alignx center, grow");

        //cancelButton = new MyButton("CANCEL", Application.turquoise, Color.white);
        resetListButton = new MyButton("RESET");
        //cancelButton.setBackground(new Color(7, 164, 121));
        //cancelButton.setForeground(new Color(250, 250, 250));
        resetListButton.addActionListener(this);
        add(resetListButton, "cell 0 3, left, gapy 5, grow");

        searchButton = new MyButton("SEARCH");
        searchButton.addActionListener(this);
        add(searchButton, "cell 1 3, right, gapy 5, grow");

        /*openFormButton = new MyButton("OPEN FILE");
        openFormButton.addActionListener(this);
        add(openFormButton, "cell 0 4, center, gapy 5, span 2, grow");
        openFormButton.setVisible(true);*/

        goBackButton = new MyButton("BACK TO MENU", Application.turquoise, Color.white);
        goBackButton.addActionListener(this);
        add(goBackButton, "cell 0 7, center, gapy 5, span 2, grow");
        goBackButton.setVisible(true);

        errorMessage = new JLabel();
        errorMessage.setFont(new Font("sansserif", Font.BOLD, 12));
        errorMessage.setForeground(Color.red);
        errorMessage.setText("Error message test");
        this.add(errorMessage, "cell 0 5, span 2, left");
        errorMessage.setVisible(false);

        scrollPane1 = new JScrollPane();
        scrollPane1.setOpaque(false);
        scrollPane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        doctorsDefListModel = new DefaultListModel<Doctor>();
        doctorsList = new JList<Doctor>(doctorsDefListModel);
        doctorsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        doctorsList.setCellRenderer(new DoctorCell());
        doctorsList.addMouseListener(this);
        scrollPane1.setViewportView(doctorsList);
        scrollPane1.setPreferredSize(this.getPreferredSize());
        add(scrollPane1,  "cell 2 1 2 6, grow, gap 10");
        //showPatients(appMain.patientMan.searchPatientsBySurname("Blanco"));
        //showDoctors(createRandomDoctors());
    }

    protected void updateDoctorDefModel(List<Doctor> doctors) {
        if(doctors == null || doctors.isEmpty()) {
            showErrorMessage("No Doctors found!");
            //openFormButton.setVisible(false);
        }else{
            if(allDoctors == null) {
                allDoctors = doctors;
            }
            //openFormButton.setVisible(true);
        }

        doctorsDefListModel.removeAllElements();
        for (Doctor r : doctors) {
            doctorsDefListModel.addElement(r);

        }
    }

    private void showErrorMessage(String message) {
        errorMessage.setText(message);
        errorMessage.setVisible(true);
    }

    private void hideErrorMessage() {
        errorMessage.setVisible(false);
    }

    private void resetPanel(){
        //TODO: reset panel when going back to menu
        hideErrorMessage();
        searchByTextField.setText("");
        doctorsDefListModel.clear();
        allDoctors = null;
    }

    private void clearView(){
        searchByTextField.setText("");
        updateDoctorDefModel(allDoctors);
        if(allDoctors.isEmpty()) {
            showErrorMessage("No Doctors found");
            //openFormButton.setVisible(false);
        }
    }

    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == goBackButton) {
            resetPanel();
            appMain.changeToMainMenu();
        }else if(e.getSource() == searchButton) {
            errorMessage.setVisible(false);
            String input = searchByTextField.getText();
            System.out.println(input);
            String search = searchByTextField.getText().trim().toLowerCase();

            List<Doctor> filteredDocs = allDoctors.stream()
                    .filter(d -> d.getSurname().toLowerCase().contains(search))
                    .collect(Collectors.toList());

            updateDoctorDefModel(filteredDocs);
            if(filteredDocs.isEmpty()) {
                showErrorMessage("No Doctor found");
                //openFormButton.setVisible(false);
            }else {
                //openFormButton.setVisible(true);
            }

        }else if(e.getSource() == resetListButton){
            updateDoctorDefModel(allDoctors);
            if(allDoctors.isEmpty()) {
                showErrorMessage("No Doctor found");
                //openFormButton.setVisible(false);
            }else {
                //openFormButton.setVisible(true);
            }
        }
        /*if(e.getSource() == searchButton) {
            errorMessage.setVisible(false);
            String input = searchByTextField.getText();
            System.out.println(input);
            List<Patient> patients = appMain.conMan.getPatientMan().searchPatientsBySurname(input);
            updatePatientDefModel(patients);
            if(patients.isEmpty()) {
                showErrorMessage("No patient found");
            }else {
                openFormButton.setVisible(true);
            }

        }else if(e.getSource() == openFormButton){
            Patient patient = patientList.getSelectedValue();
            if(patient == null) {
                showErrorMessage("No patient Selected");
            }else {
                resetPanel();
                appMain.changeToAdmitPatient(patient);
            }
        }else if(e.getSource() == cancelButton){
            resetPanel();
            appMain.changeToRecepcionistMenu();
        }*/

    }


    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}