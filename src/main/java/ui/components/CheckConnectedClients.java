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

public class CheckConnectedClients extends JPanel implements MouseListener {
    private JLabel errorMessage;

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

    public void showErrorMessage(String text) {
        errorMessage.setVisible(true);
        errorMessage.setText(text);
    }

    public void initList(ArrayList<String> connectedClients) {
        //JPanel gridPanel = new JPanel(new GridLayout(patients.size(), 0));

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


