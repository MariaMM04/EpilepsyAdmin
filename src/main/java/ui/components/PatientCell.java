package ui.components;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;

import net.miginfocom.swing.MigLayout;
import org.example.entities_medicaldb.Doctor;
import org.example.entities_medicaldb.Patient;
import ui.windows.Application;

import java.awt.*;

/**
 * List cell renderer for displaying patient's information in a custom layout.
 * It formats multiple fields such as name, birthday, contact information,
 * doctor assigned, and status.
 */
public class PatientCell implements ListCellRenderer<Patient> {

    private final Color titleColor = Application.turquoise;
    private final Font titleFont = new Font("sansserif", 3, 12);
    private final Font contentFont = new Font("sansserif", 1, 12);
    private final Color contentColor = new Color(122, 140, 141);
    //private Color backgroundColor = new Color(230, 245, 241);

    /**
     * Returns a custom component used to render a single patient entry in the list.
     *
     * @param list the list containing the cell
     * @param value the patient object to render
     * @param index the index of the cell in the list
     * @param isSelected whether the cell is selected
     * @param cellHasFocus whether the cell has focus
     * @return the component used for rendering
     */
    @Override
    public Component getListCellRendererComponent(JList<? extends Patient> list, Patient value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {

        JPanel listCell = new JPanel();
        listCell.setLayout(new MigLayout("fill, inset 20, gap 0, wrap 4", "[][]5[][]", "[][][][]"));
        listCell.setBackground(Color.white);
        Border border = javax.swing.BorderFactory.createLineBorder(Application.dark_turquoise);
        listCell.setBorder(border);

        //Name
        JLabel nameHeading = new JLabel("Name and Surname:");
        nameHeading.setForeground(titleColor);
        nameHeading.setFont(titleFont);

        //value.getName()+" "+value.getSurname()

        JLabel nameText = new JLabel(value.getName()+" "+value.getSurname());
        nameText.setForeground(contentColor);
        nameText.setFont(contentFont);
        listCell.add(nameHeading, "grow, left");
        listCell.add(nameText, "grow, left");

        JLabel ageHeading = new JLabel("Birthday:");
        ageHeading.setForeground(titleColor);
        ageHeading.setFont(titleFont);
        JLabel ageText = new JLabel(value.getDateOfBirth().toString());
        ageText.setForeground(contentColor);
        ageText.setFont(contentFont);
        JLabel genderHeading = new JLabel("Gender:");
        genderHeading.setForeground(titleColor);
        genderHeading.setFont(titleFont);
        JLabel genderText = new JLabel(value.getGender());
        genderText.setForeground(contentColor);
        genderText.setFont(contentFont);
        JLabel phoneHeading = new JLabel("Phone Number:");
        phoneHeading.setForeground(titleColor);
        phoneHeading.setFont(titleFont);
        JLabel phoneText = new JLabel(value.getContact());
        phoneText.setForeground(contentColor);
        phoneText.setFont(contentFont);
        JLabel emailHeading = new JLabel("Email:");
        emailHeading.setForeground(titleColor);
        emailHeading.setFont(titleFont);
        JLabel emailText = new JLabel(value.getEmail());
        emailText.setForeground(contentColor);
        emailText.setFont(contentFont);

        listCell.add(ageHeading, "grow, left");
        listCell.add(ageText, "grow, left");
        listCell.add(genderHeading, "grow, left");
        listCell.add(genderText, "grow, left");
        listCell.add(phoneHeading, "grow, left");
        listCell.add(phoneText, "grow, left");
        listCell.add(emailHeading, "grow, left");
        listCell.add(emailText, "grow, left");

        JLabel doctorHeading = new JLabel("Doctor Assigned:");
        doctorHeading.setForeground(titleColor);
        doctorHeading.setFont(titleFont);
        JLabel doctorText = new JLabel("None");
        doctorText.setForeground(contentColor);
        doctorText.setFont(contentFont);
        JLabel statusHeading = new JLabel("Status:");
        statusHeading.setForeground(titleColor);
        statusHeading.setFont(titleFont);
        JLabel statusTxt = new JLabel(String.valueOf(value.isActive()));
        statusTxt.setForeground(contentColor);
        statusTxt.setFont(contentFont);

        if(value.getDoctor() != null){
            Doctor doctor = value.getDoctor();
            doctorText.setText(doctor.getName()+" "+doctor.getSurname());
        }

        listCell.add(doctorHeading, "grow, left");
        listCell.add(doctorText, "grow, left");
        listCell.add(statusHeading, "grow, left");
        listCell.add(statusTxt, "grow, left");

        if(isSelected)
        {
            listCell.setBackground(Application.lighter_turquoise);
        }else {
            listCell.setBackground(Color.white);
        }
        return listCell;
    }

}