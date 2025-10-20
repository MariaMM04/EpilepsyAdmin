package ui.components;

import net.miginfocom.swing.MigLayout;
import ui.windows.Application;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class MenuTemplate extends JPanel implements ActionListener {

    private static final long serialVersionUID = 1L;
    protected JPanel coverPanel;
    protected PanelMenu panelMenu;
    protected ArrayList<JButton> buttons;
    private final Color backgroundColor2 = Color.WHITE;


    public MenuTemplate() {
        this.setLayout(new MigLayout("fill, inset 0, gap 0", "[][][][][]", "[30%][20%][20%][20%][20%]"));
        buttons = new ArrayList<JButton>();
    }

    protected void init(ImageIcon logo, String company_name) {
        coverPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g); // pinta el fondo base

                Graphics2D g2d = (Graphics2D) g.create();
                int width = getWidth();
                int height = getHeight();

                // Degradado de izquierda a derecha (puedes cambiarlo a vertical si quieres)
                GradientPaint gradient = new GradientPaint(0, 0, Application.light_purple, 0, getHeight(), Application.light_turquoise);

                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, width, height);
                g2d.dispose();
            }
        };
        initCover(logo, company_name);

        panelMenu = new PanelMenu(buttons);
        for (JButton jButton : buttons) {
            jButton.addActionListener(this);
        }
        panelMenu.setBackground(backgroundColor2);
        this.add(coverPanel, "cell 0 0 5 1,grow");
        this.add(panelMenu, "cell 0 1 5 5,grow");
    }

    private void initCover(ImageIcon logo, String company_name) {
        coverPanel.setLayout(new MigLayout("fill", "2%[10%]2%[90%]", ""));
        JLabel picLabel = new JLabel();
        picLabel.setIcon(logo);
        coverPanel.add(picLabel, "cell 0 0,align center");

        JLabel title = new JLabel(company_name);
        title.setFont(new Font("sansserif", 1, 20));
        title.setForeground(Application.dark_purple);
        coverPanel.add(title, "cell 1 0, align left, growx 0");
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }

}
