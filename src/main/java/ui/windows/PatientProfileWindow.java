package ui.windows;

import org.example.entities_medicaldb.Patient;
import org.example.JDBC.medicaldb.PatientJDBC;
import ui.components.MenuTemplate;
import ui.components.MyButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class PatientProfileWindow extends MenuTemplate {
    private static final long serialVersionUID = 1L;

    // Campos
    private JTextField txtNombre, txtApellido, txtGenero, txtFecha, txtContacto, txtEmail;
    private JLabel lblMensaje;
    private MyButton btnGuardar;
    private ImageIcon logoIcon;
    private String titleText;
    protected JPanel panelContent;
    protected JPanel buttons;

    public PatientProfileWindow() {

        titleText = "PATIENT PROFILE";
        logoIcon = new ImageIcon(getClass().getResource("/icons/night_guardian_mini_128.png"));

        addComponents();
        this.init(logoIcon, titleText);
    }

    private void addComponents() {

        JLabel lblNombre = new JLabel("Nombre:");
        JLabel lblApellido = new JLabel("Apellido:");
        JLabel lblGenero = new JLabel("Género:");
        JLabel lblFecha = new JLabel("Fecha de nacimiento (YYYY-MM-DD):");
        JLabel lblContacto = new JLabel("Teléfono:");
        JLabel lblEmail = new JLabel("E-mail:");
        lblMensaje = new JLabel("", SwingConstants.CENTER);


        txtNombre = new JTextField();
        txtApellido = new JTextField();
        txtGenero = new JTextField();
        txtFecha = new JTextField();
        txtContacto = new JTextField();
        txtEmail = new JTextField();


        btnGuardar = new MyButton("Guardar cambios");

        btnGuardar.addActionListener((ActionEvent e) -> {
            try {
                Patient p = new Patient();
                p.setName(txtNombre.getText());
                p.setSurname(txtApellido.getText());
                p.setGender(txtGenero.getText());
                p.setEmail(txtEmail.getText());
                p.setContact(txtContacto.getText());

                try {
                    if (!txtFecha.getText().isEmpty()) {
                        LocalDate fecha = LocalDate.parse(txtFecha.getText());
                        p.setDateOfBirth(fecha);
                    }
                } catch (DateTimeParseException exFecha) {
                    lblMensaje.setText("Fecha inválida (use YYYY-MM-DD)");
                    lblMensaje.setForeground(Color.RED);
                    return;
                }


                if (p.getName().isEmpty() || p.getGender().isEmpty() || p.getContact().isEmpty()) {
                    lblMensaje.setText("Error updating profile");
                    lblMensaje.setForeground(Color.RED);
                } else {
                    PatientJDBC patientJDBC = new PatientJDBC();
                    patientJDBC.updatePatient(p);
                    lblMensaje.setText("Profile updated");
                    lblMensaje.setForeground(new Color(0, 128, 0));
                }

            } catch (Exception ex) {
                lblMensaje.setText("Error updating profile");
                lblMensaje.setForeground(Color.RED);
            }
        });


        panelContent.setLayout(new GridLayout(7, 2, 10, 10));
        panelContent.add(lblNombre); panelContent.add(txtNombre);
        panelContent.add(lblApellido); panelContent.add(txtApellido);
        panelContent.add(lblGenero); panelContent.add(txtGenero);
        panelContent.add(lblFecha); panelContent.add(txtFecha);
        panelContent.add(lblContacto); panelContent.add(txtContacto);
        panelContent.add(lblEmail); panelContent.add(txtEmail);
        panelContent.add(new JLabel("")); panelContent.add(lblMensaje);

        buttons.add(btnGuardar);
    }
}
