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
 * Custom Swing ListCellRenderer used for displaying doctor information
 * in a formatted multi-line layout inside a JList.
 * <p>
 * It shows the doctor's name, speciality, department, contact information
 * and active status.
 *
 *  @author MamenCortes
 */
public class DoctorCell implements ListCellRenderer<Doctor> {

    private final Color titleColor = Application.turquoise;
    private final Font titleFont = new Font("sansserif", 3, 12);
    private final Font contentFont = new Font("sansserif", 1, 12);
    private final Color contentColor = new Color(122, 140, 141);
    //private Color backgroundColor = new Color(230, 245, 241);


    @Override
    public Component getListCellRendererComponent(JList<? extends Doctor> list, Doctor value, int index,
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

        JLabel ageHeading = new JLabel("Speciality:");
        ageHeading.setForeground(titleColor);
        ageHeading.setFont(titleFont);
        JLabel ageText = new JLabel(value.getSpeciality());
        ageText.setForeground(contentColor);
        ageText.setFont(contentFont);
        JLabel genderHeading = new JLabel("Address/Department:");
        genderHeading.setForeground(titleColor);
        genderHeading.setFont(titleFont);
        JLabel genderText = new JLabel(value.getDepartment());
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
        JLabel statusHeading = new JLabel("Status:");
        statusHeading.setForeground(titleColor);
        statusHeading.setFont(titleFont);
        JLabel statusTxt = new JLabel(String.valueOf(value.isActive()));
        statusTxt.setForeground(contentColor);
        statusTxt.setFont(contentFont);

        listCell.add(ageHeading, "grow, left");
        listCell.add(ageText, "grow, left");
        listCell.add(genderHeading, "grow, left");
        listCell.add(genderText, "grow, left");
        listCell.add(phoneHeading, "grow, left");
        listCell.add(phoneText, "grow, left");
        listCell.add(emailHeading, "grow, left");
        listCell.add(emailText, "grow, left");
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
