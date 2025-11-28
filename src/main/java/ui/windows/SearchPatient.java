package ui.windows;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import net.miginfocom.swing.MigLayout;
import org.example.entities_medicaldb.Doctor;
import org.example.entities_medicaldb.Patient;
import org.example.service.AdminLinkService;
import ui.components.MyButton;
import ui.components.MyTextField;
import ui.components.PatientCell;
import javax.swing.*;
/**
 * Panel that allows administrators to search, filter and manage the list
 * of patients stored in the Night Guardian system.
 * <p>
 * It offers dynamic filtering by surname, resetting the full list, toggling
 * activation status, and navigating back to the main menu. Patients are
 * displayed in a scrollable list using a custom {@link PatientCell} renderer.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Display all patients retrieved externally</li>
 *     <li>Filter patients by surname (case-insensitive)</li>
 *     <li>Toggle activation status of a selected patient</li>
 *     <li>Allow navigation back to the {@link MainMenu}</li>
 *     <li>Show error messages when applicable</li>
 * </ul>
 *
 * <h2>Lifecycle</h2>
 * <ol>
 *     <li>Constructor receives the main {@link Application} controller.</li>
 *     <li>{@link #initMainPanel()} builds all UI elements:
 *         <ul>
 *             <li>Title + search bar</li>
 *             <li>Search and reset buttons</li>
 *             <li>Status toggle button</li>
 *             <li>Scrollable list of patients</li>
 *             <li>Back to menu button</li>
 *         </ul>
 *     </li>
 *     <li>The external caller invokes {@link #updatePatientDefModel(List)} to populate the list.</li>
 *     <li>User interacts with search/reset/status buttons.</li>
 *     <li>Returning to main menu triggers a full panel reset.</li>
 * </ol>
 *
 * <h2>Status Management</h2>
 * <p>
 * The user can toggle a patient's {@code active} flag using the button
 * <b>SWITCH STATUS</b>, which internally calls:
 * {@link AdminLinkService#changePatientStatus(String, Boolean)}.
 * </p>
 * <p>
 * After successful status change, the list updates and a green message is displayed.
 * </p>
 *
 * @author MamenCortes
 */
public class SearchPatient extends JPanel implements ActionListener, MouseListener {

    protected Application appMain;
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
    protected MyButton switchStatus;
    protected JLabel errorMessage;
    protected MyButton goBackButton;
    protected JList<Patient> patientsList;
    protected DefaultListModel<Patient> patientsDefListModel;
    protected List<Patient> allPatients;

    /**
     * Constructs the SearchPatient panel and initializes the UI.
     *
     * @param appMain main application controller for navigation and admin actions
     */
    public SearchPatient(Application appMain) {
        this.appMain = appMain;
        initMainPanel();
    }

    /**
     * Builds the complete UI of the panel using MigLayout.
     * <p>
     * The layout includes:
     * <ul>
     *     <li>Title label and icon</li>
     *     <li>Search panel with text field + search/reset buttons</li>
     *     <li>Status toggle button to activate/deactivate selected patient</li>
     *     <li>Scrollable list with custom {@link PatientCell} renderer</li>
     *     <li>Back to menu button</li>
     *     <li>Error message label</li>
     * </ul>
     */
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

        resetListButton = new MyButton("RESET");
        resetListButton.addActionListener(this);
        add(resetListButton, "cell 0 3, left, gapy 5, grow");

        searchButton = new MyButton("SEARCH");
        searchButton.addActionListener(this);
        add(searchButton, "cell 1 3, right, gapy 5, grow");

        switchStatus = new MyButton("DEACTIVATE USER");
        switchStatus.addActionListener(this);
        add(switchStatus, "cell 0 4, center, gapy 5, span 2, grow");

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
    /**
     * Updates the list model with the provided patients.
     * <p>
     * The first time it is called, the list is cached into {@link #allPatients}.
     * Afterwards, it:
     * <ul>
     *     <li>Clears and reloads the visible list</li>
     *     <li>Shows an error if the list is empty</li>
     * </ul>
     *
     * @param patients the patients to display
     */
    protected void updatePatientDefModel(List<Patient> patients) {
        if(patients == null || patients.isEmpty()) {
            showErrorMessage("No patients found!");
        }else{
            if(allPatients == null) {
                allPatients = patients;
            }
        }

        patientsDefListModel.removeAllElements();
        for (Patient r : patients) {
            patientsDefListModel.addElement(r);

        }
    }
    /**
     * Shows an error message in red under the search panel.
     *
     * @param message message to show
     */
    private void showErrorMessage(String message) {
        errorMessage.setText(message);
        errorMessage.setVisible(true);
        errorMessage.setForeground(Color.red);
    }
    /**
     * Hides the error message.
     */
    private void hideErrorMessage() {
        errorMessage.setVisible(false);
    }
    /**
     * Resets all components when leaving this panel:
     * <ul>
     *     <li>Clears the search field</li>
     *     <li>Clears list contents</li>
     *     <li>Clears cached patient list</li>
     *     <li>Hides error messages</li>
     * </ul>
     */
    private void resetPanel(){
        hideErrorMessage();
        searchByTextField.setText("");
        allPatients = null;
        patientsDefListModel.clear();
    }
    /**
     * Restores the list to its unfiltered state.
     */
    private void clearView(){
        searchByTextField.setText("");
        updatePatientDefModel(allPatients);
        if(allPatients.isEmpty()) {
            showErrorMessage("No patient found");
        }
    }
    /**
     * Handles interaction with all panel buttons:
     * <ul>
     *     <li><b>BACK TO MENU:</b> resets panel and returns to main menu</li>
     *     <li><b>SEARCH:</b> filters by surname</li>
     *     <li><b>RESET:</b> shows full list</li>
     *     <li><b>SWITCH STATUS:</b> toggles a patient's activation state</li>
     * </ul>
     *
     * @param e the action event triggered by user interaction
     */
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
            }
        }else if(e.getSource() == resetListButton) {
            updatePatientDefModel(allPatients);
            if (allPatients.isEmpty()) {
                showErrorMessage("No patient found");
            }
        }else if (e.getSource() == switchStatus) {
            Patient selectedPatient = patientsList.getSelectedValue();
            if(selectedPatient == null) {
                showErrorMessage("No Patient selected");
            }else {
                //IF true change to false
                //IF false change to true
                Boolean result = false;
                try{
                    result = appMain.adminLinkService.changePatientStatus(selectedPatient.getEmail(),false);
                } catch (SQLException ex) {
                }
                if(result) {
                    selectedPatient.setActive(!selectedPatient.isActive());
                    updatePatientDefModel(allPatients);
                    showErrorMessage(selectedPatient.getName()+" "+selectedPatient.getSurname()+" has been deactivated");
                    errorMessage.setForeground(Color.green);
                }else {
                    showErrorMessage("Error changing status");
                }

            }
        }

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