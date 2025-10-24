package ui.windows;

import org.example.entities_medicaldb.Doctor;
import org.example.JDBC.medicaldb.DoctorJDBC;
import ui.components.MenuTemplate;
import ui.components.MyButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class DoctorProfileWindow extends MenuTemplate {
    private static final long serialVersionUID = 1L;


    private JTextField txtNombre, txtApellido, txtEmail, txtContacto;
    private JLabel lblMensaje;
    private MyButton btnGuardar;
    private ImageIcon logoIcon;
    private String titleText;
    protected JPanel panelContent;
    protected JPanel buttons;


    public DoctorProfileWindow() {
        // Configuración inicial
        titleText = "DOCTOR PROFILE";
        logoIcon = new ImageIcon(getClass().getResource("/icons/night_guardian_mini_128.png"));

        addComponents();
        this.init(logoIcon, titleText);
    }

    private void addComponents() {
        // Campos de texto
        txtNombre = new JTextField();
        txtApellido = new JTextField();
        txtEmail = new JTextField();
        txtContacto = new JTextField();


        JLabel lblNombre = new JLabel("Nombre:");
        JLabel lblApellido = new JLabel("Apellido:");
        JLabel lblEmail = new JLabel("E-mail:");
        JLabel lblContacto = new JLabel("Teléfono:");
        lblMensaje = new JLabel("", SwingConstants.CENTER);


        btnGuardar = new MyButton("Guardar cambios");


        btnGuardar.addActionListener((ActionEvent e) -> {
            try {
                Doctor d = new Doctor();
                d.setName(txtNombre.getText());
                d.setSurname(txtApellido.getText());
                d.setEmail(txtEmail.getText());
                d.setContact(txtContacto.getText());


                if (d.getName().isEmpty() || d.getContact().isEmpty() || d.getEmail().isEmpty()) {
                    lblMensaje.setText("Error updating profile");
                    lblMensaje.setForeground(Color.RED);
                } else {
                    //DoctorJDBC doctorJDBC = new DoctorJDBC();
                    //doctorJDBC.updateDoctor(d);
                    lblMensaje.setText("Profile updated");
                    lblMensaje.setForeground(new Color(0, 128, 0));
                }
            } catch (Exception ex) {
                lblMensaje.setText("Error updating profile");
                lblMensaje.setForeground(Color.RED);
            }
        });


        panelContent.setLayout(new GridLayout(5, 2, 10, 10));
        panelContent.add(lblNombre); panelContent.add(txtNombre);
        panelContent.add(lblApellido); panelContent.add(txtApellido);
        panelContent.add(lblEmail); panelContent.add(txtEmail);
        panelContent.add(lblContacto); panelContent.add(txtContacto);
        panelContent.add(new JLabel("")); panelContent.add(lblMensaje);

        buttons.add(btnGuardar);
    }
}
