package ui.windows;


import org.example.entities_medicaldb.Patient;
import org.example.JDBC.medicaldb.PatientJDBC;

import javax.swing.*;
import java.awt.*;

public class PatientProfileWindow extends JFrame {

    public PatientProfileWindow() {
        setTitle("Perfil del Paciente");
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(7, 2));

        JLabel lblNombre = new JLabel("Nombre:");
        JTextField txtNombre = new JTextField();

        JLabel lblApellido = new JLabel("Apellido:");
        JTextField txtApellido = new JTextField();

        JLabel lblSexo = new JLabel("Sexo:");
        JTextField txtSexo = new JTextField();

        JLabel lblEdad = new JLabel("Edad / Fecha de nacimiento:");
        JTextField txtEdad = new JTextField();

        JLabel lblTelefono = new JLabel("Teléfono:");
        JTextField txtTelefono = new JTextField();

        JLabel lblEmail = new JLabel("E-mail:");
        JTextField txtEmail = new JTextField();

        JButton btnGuardar = new JButton("Guardar cambios");

        add(lblNombre); add(txtNombre);
        add(lblApellido); add(txtApellido);
        add(lblSexo); add(txtSexo);
        add(lblEdad); add(txtEdad);
        add(lblTelefono); add(txtTelefono);
        add(lblEmail); add(txtEmail);
        add(btnGuardar);
    }
}
