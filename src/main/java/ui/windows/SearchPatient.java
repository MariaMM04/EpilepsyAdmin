package ui.windows;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.stream.Collectors;

import net.miginfocom.swing.MigLayout;
import org.example.entities_medicaldb.Patient;
import ui.components.MyButton;
import ui.components.MyTextField;
import ui.components.PatientCell;
import javax.swing.*;

public class SearchPatient extends JPanel implements ActionListener, MouseListener {

    private Application appMain;
    protected final Font titleFont = new Font("sansserif", 3, 15);
    protected final Color titleColor = Application.dark_purple;
    protected JLabel title;
    protected String titleText = " Search Patients ";
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
    protected JList<Patient> patientsList;
    protected DefaultListModel<Patient> patientsDefListModel;
    protected List<Patient> allPatients;

    public SearchPatient(Application appMain) {
        this.appMain = appMain;
        initMainPanel();
        /*List<Patient> patients = new ArrayList<>();
        for(int i = 0; i < 10; i++) {
            patients.add(RandomData.generateRandomPatient());
        }
        //showPatients(patients);
        System.out.println("Search Patient Panel Successfully created");*/
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

        patientsDefListModel = new DefaultListModel<Patient>();
        patientsList = new JList<Patient>(patientsDefListModel);
        patientsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        patientsList.setCellRenderer(new PatientCell());
        patientsList.addMouseListener(this);
        scrollPane1.setViewportView(patientsList);
        scrollPane1.setPreferredSize(this.getPreferredSize());

        add(scrollPane1,  "cell 2 1 2 6, grow, gap 10");
    }

    protected void updatePatientDefModel(List<Patient> patients) {
        if(patients == null || patients.isEmpty()) {
            showErrorMessage("No patients found!");
            //openFormButton.setVisible(false);
        }else{
            if(allPatients == null) {
                allPatients = patients;
            }
            //openFormButton.setVisible(true);
        }

        patientsDefListModel.removeAllElements();
        for (Patient r : patients) {
            patientsDefListModel.addElement(r);

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
        allPatients = null;
        patientsDefListModel.clear();
    }

    private void clearView(){
        searchByTextField.setText("");
        updatePatientDefModel(allPatients);
        if(allPatients.isEmpty()) {
            showErrorMessage("No patient found");
            //openFormButton.setVisible(false);
        }else {
            //openFormButton.setVisible(true);
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

            List<Patient> filteredPatients = allPatients.stream()
                    .filter(p -> p.getSurname().toLowerCase().contains(search))
                    .collect(Collectors.toList());

            updatePatientDefModel(filteredPatients);
            if (filteredPatients.isEmpty()) {
                showErrorMessage("No patient found");
                //openFormButton.setVisible(false);
            } else {
                //openFormButton.setVisible(true);
            }
        }else if(e.getSource() == resetListButton) {
            updatePatientDefModel(allPatients);
            if (allPatients.isEmpty()) {
                showErrorMessage("No patient found");
                //openFormButton.setVisible(false);
            } else {
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