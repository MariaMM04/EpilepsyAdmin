package ui.components;

import ui.windows.Application;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;

import java.awt.*;
import java.awt.image.BufferedImage;

public class MyButton extends JButton {

    private static final long serialVersionUID = 5848952178038888829L;

    private final Color backgroundColor = Application.light_purple;
    private final Color foregroundColor = Application.darker_purple;


    private ImageIcon image;
    private final Font font = new Font("sansserif", 1, 15);

    public MyButton(String text, String imageSource){
        try {
            Image img = ImageIO.read(getClass().getResource(imageSource));
            image = new ImageIcon(img);
            this.setIcon(image);
        } catch (Exception ex) {
            System.out.println(ex);
        }
        this.setText(text);
        this.setBackground(backgroundColor);
        this.setForeground(foregroundColor.darker());
        //System.out.println(foregroundColor.darker());
        this.setFont(font);
        this.setUI(new StyledButtonUI());
    }

    public MyButton(String text) {
        this.setBackground(backgroundColor);
        //this.setForeground(foregroundColor.darker());
        this.setForeground(foregroundColor);
        this.setText(text);
        this.setFont(font);
        this.setUI(new StyledButtonUI());
    }

    public MyButton(String text, Color backgroundColor, Color foregroundColor) {
        this.setBackground(backgroundColor);
        this.setForeground(foregroundColor);
        this.setText(text);
        this.setFont(font);
        this.setUI(new StyledButtonUI());
    }

    public MyButton() {
        this.setBackground(backgroundColor);
        this.setForeground(foregroundColor.darker());
        this.setText("");
        this.setFont(font);
        this.setUI(new StyledButtonUI());
    }

    @Override
    protected void paintComponent(Graphics grphcs) {
        int width = getWidth();
        int height = getHeight();
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, width, height, height, height);
        /*if (pressedPoint != null) {
            g2.setColor(effectColor);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
            g2.fillOval((int) (pressedPoint.x - animatSize / 2), (int) (pressedPoint.y - animatSize / 2), (int) animatSize, (int) animatSize);
        }*/
        g2.dispose();
        grphcs.drawImage(img, 0, 0, null);
        super.paintComponent(grphcs);
    }


}

