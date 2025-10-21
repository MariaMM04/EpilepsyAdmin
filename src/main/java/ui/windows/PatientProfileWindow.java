package org.example.ui.windows;

import org.example.entities_medicaldb.Patient;
import org.example.JDBC.medicaldb.PatientJDBC;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class PatientProfileWindow extends JFrame {

    public PatientProfileWindow() {
        setTitle("Perfil del Paciente");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(7, 2));

        // --- Campos del formulario ---
        JLabel lblNombre = new JLabel("Nombre:");
        JTextField txtNombre = new JTextField();

        JLabel lblApellido = new JLabel("Apellido:");
        JTextField txtApellido = new JTextField();

        JLabel lblGenero = new JLabel("Género:");
        JTextField txtGenero = new JTextField();

        JLabel lblFecha = new JLabel("Fecha de nacimiento (YYYY-MM-DD):");
        JTextField txtFecha = new JTextField();

        JLabel lblContacto = new JLabel("Teléfono:");
        JTextField txtContacto = new JTextField();

        JLabel lblEmail = new JLabel("E-mail:");
        JTextField txtEmail = new JTextField();

        JButton btnGuardar = new JButton("Guardar cambios");
        JLabel lblMensaje = new JLabel("", SwingConstants.CENTER);


        btnGuardar.addActionListener(e -> {
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
                    lblMensaje.setText("Fecha inválida. Use formato YYYY-MM-DD");
                    lblMensaje.setForeground(Color.RED);
                    return; // no continúa si la fecha está mal
                }


                if (p.getName().isEmpty() || p.getGender().isEmpty() || p.getContact().isEmpty()) {
                    lblMensaje.setText(" Error updating profile: faltan campos obligatorios");
                    lblMensaje.setForeground(Color.RED);
                } else {

                    PatientJDBC patientJDBC = new PatientJDBC();
                    patientJDBC.updatePatient(p);
                    lblMensaje.setText("Profile updated");
                    lblMensaje.setForeground(new Color(0, 128, 0)); // verde
                }

            } catch (Exception ex) {
                lblMensaje.setText("Error updating profile");
                lblMensaje.setForeground(Color.RED);
            }
        });


        add(lblNombre); add(txtNombre);
        add(lblApellido); add(txtApellido);
        add(lblGenero); add(txtGenero);
        add(lblFecha); add(txtFecha);
        add(lblContacto); add(txtContacto);
        add(lblEmail); add(txtEmail);
        add(btnGuardar); add(lblMensaje);
    }
}
