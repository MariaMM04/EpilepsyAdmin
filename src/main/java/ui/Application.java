package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.util.ArrayList;

public class Application extends JFrame {
    public static void main(String[] args) {
        Application app = new Application();
        app.setVisible(true);
    }

    public Application() {
        initComponents();
        setBounds(100, 100, 602, 436);
    }

    public void initComponents() {
        setTitle("Application");
        //setSize(602, 436);
        setLayout(null);
        setLocationRelativeTo(null);
        setIconImage(new ImageIcon(getClass().getResource("/icons/night_guardian_mini_500.png")).getImage());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }
}
