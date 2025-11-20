package ui.components;

import net.miginfocom.swing.MigLayout;
import org.example.entities_medicaldb.Doctor;
import ui.windows.Application;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

/**
 * Panel that displays the list of currently connected clients to the server.
 * <p>
 * The panel provides a scrollable list, a "Stop Server" button,
 * and a "Go Back" button. It also supports showing error messages.
 */
public class CheckConnectedClients extends JPanel implements MouseListener {
    private JLabel errorMessage;

    /**
     * Creates a new CheckConnectedClients panel.
     *
     * @param connectedClients list of connected client descriptions
     * @param goBackButton     button that returns to the previous menu
     * @param stopServerButton button to stop the server
     */
    public CheckConnectedClients(ArrayList<String> connectedClients, MyButton goBackButton, MyButton stopServerButton) {
        this.setLayout(new MigLayout("wrap, fill", "push[center]push", "push[]25[]10[]10[]push"));
        JLabel label = new JLabel("Connected Clients: "+connectedClients.size());
        label.setFont(new Font("sansserif", 1, 30));
        label.setForeground(Application.dark_purple);
        this.add(label);

        JScrollPane scrollPane1 = new JScrollPane();
        scrollPane1.setOpaque(false);
        scrollPane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        //scrollPane1.setViewportView(gridPanel);

        DefaultListModel<String> clientsDefListModel = new DefaultListModel<String>();
        if(connectedClients != null) {
            for (String r : connectedClients) {
                clientsDefListModel.addElement(r);

            }
        }

        JList clientsList = new JList<String>(clientsDefListModel);
        clientsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        //clientsList.setCellRenderer(new DoctorCell());
        clientsList.addMouseListener(this);
        scrollPane1.setViewportView(clientsList);

        scrollPane1.setPreferredSize(this.getPreferredSize());

        add(scrollPane1,  "w 60%");

        errorMessage = new JLabel();
        errorMessage.setFont(new Font("sansserif", Font.BOLD, 12));
        errorMessage.setForeground(Color.red);
        errorMessage.setText("Error message test");
        errorMessage.setVisible(false);


        MyButton stopServerBt = stopServerButton;
        stopServerBt.setText("STOP SERVER");
        stopServerBt.setBackground(Application.turquoise);
        stopServerBt.setForeground(Color.white);
        MyButton goBackBt = goBackButton;
        goBackBt.setText("GO BACK");
        goBackBt.setBackground(Application.turquoise);
        goBackBt.setForeground(Color.white);

        this.add(errorMessage,"w 10%" );
        this.add(stopServerBt, "split 2, grow, left");
        this.add(goBackBt, "grow, right");


    }
    /**
     * Displays an error message to the user.
     *
     * @param text the message to display
     */
    public void showErrorMessage(String text) {
        errorMessage.setVisible(true);
        errorMessage.setText(text);
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}


