package ui.windows;
import org.example.entities_medicaldb.Doctor;
import javax.swing.*;
import java.awt.*;

public class DoctorProfileWindow extends JFrame {

    public DoctorProfileWindow() {
        setTitle("Perfil del Doctor");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(4, 2));

        JLabel lblNombre = new JLabel("Nombre:");
        JTextField txtNombre = new JTextField();

        JLabel lblApellido = new JLabel("Apellido:");
        JTextField txtApellido = new JTextField();

        JLabel lblTelefono = new JLabel("Teléfono:");
        JTextField txtTelefono = new JTextField();

        JButton btnGuardar = new JButton("Guardar cambios");

        add(lblNombre); add(txtNombre);
        add(lblApellido); add(txtApellido);
        add(lblTelefono); add(txtTelefono);
        add(btnGuardar);
    }
}

