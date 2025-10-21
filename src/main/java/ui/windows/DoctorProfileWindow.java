package org.example.ui.windows;

import org.example.entities_medicaldb.Doctor;
import org.example.JDBC.medicaldb.DoctorJDBC;

import javax.swing.*;
import java.awt.*;

public class DoctorProfileWindow extends JFrame {

    public DoctorProfileWindow() {
        setTitle("Perfil del Doctor");
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(6, 2));


        JLabel lblNombre = new JLabel("Nombre:");
        JTextField txtNombre = new JTextField();

        JLabel lblApellido = new JLabel("Apellido:");
        JTextField txtApellido = new JTextField();

        JLabel lblEmail = new JLabel("E-mail:");
        JTextField txtEmail = new JTextField();

        JLabel lblContacto = new JLabel("Teléfono:");
        JTextField txtContacto = new JTextField();

        JButton btnGuardar = new JButton("Guardar cambios");
        JLabel lblMensaje = new JLabel("", SwingConstants.CENTER);


        btnGuardar.addActionListener(e -> {
            try {
                Doctor d = new Doctor();
                d.setName(txtNombre.getText());
                d.setSurname(txtApellido.getText());
                d.setEmail(txtEmail.getText());
                d.setContact(txtContacto.getText());


                if (d.getName().isEmpty() || d.getContact().isEmpty() || d.getEmail().isEmpty()) {
                    lblMensaje.setText("Error updating profile: faltan campos obligatorios");
                    lblMensaje.setForeground(Color.RED);
                } else {
                    DoctorJDBC doctorJDBC = new DoctorJDBC();
                    doctorJDBC.updateDoctor(d);
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
        add(lblEmail); add(txtEmail);
        add(lblContacto); add(txtContacto);
        add(btnGuardar); add(lblMensaje);
    }
}
