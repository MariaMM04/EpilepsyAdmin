package ui.components;

import ui.windows.Application;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTextField;
/**
 * Custom JTextField with rounded background, hint (placeholder) support,
 * optional icons, and themed styling.
 */
public class MyTextField extends JTextField{

    private static final long serialVersionUID = 5288012491687840392L;
    private Icon prefixIcon;
    private Icon suffixIcon;
    private String hint = "";
    private Color backgroundColor = Application.lighter_turquoise;
    //private Color backgroundColor = Application.light_purple;
    private Color selectionColor;
    private Color contentColor = Application.darker_turquoise;
    /**
     * Returns the placeholder text shown when the field is empty.
     *
     * @return the hint text
     */
    public String getHint() {
        return hint;
    }
    /**
     * Sets the placeholder text shown when the field is empty.
     *
     * @param hint the hint text
     */
    public void setHint(String hint) {
        this.hint = hint;
    }
    /**
     * Returns the prefix icon displayed on the left side of the text field.
     *
     * @return prefixIcon the icon to display
     */
    public Icon getPrefixIcon() {
        return prefixIcon;
    }
    /**
     * Sets a prefix icon displayed on the left side of the text field.
     *
     * @param prefixIcon the icon to display
     */
    public void setPrefixIcon(Icon prefixIcon) {
        this.prefixIcon = prefixIcon;
        initBorder();
    }
    /**
     * Returns the suffix icon displayed on the left side of the text field.
     *
     * @return suffixIcon the icon to display
     */
    public Icon getSuffixIcon() {
        return suffixIcon;
    }
    /**
     * Sets a suffix icon displayed on the right side of the text field.
     *
     * @param suffixIcon the icon to display
     */
    public void setSuffixIcon(Icon suffixIcon) {
        this.suffixIcon = suffixIcon;
        initBorder();
    }
    /**
     * Creates an empty styled text field without hint text.
     */
    public MyTextField() {
        setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(new Color(0, 0, 0, 0));
        //setForeground(Color.decode("#7A8C8D"));
        setForeground(contentColor);
        setFont(new java.awt.Font("sansserif", 0, 13));
        setSelectionColor(new Color(75, 175, 152));
        setDisabledTextColor(Color.decode("#7A8C8D").darker());
    }
    /**
     * Creates a styled text field with the given hint text displayed
     * when no content has been typed.
     *
     * @param hint placeholder text
     */
    public MyTextField(String hint) {
        setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(new Color(0, 0, 0, 0));
        //setForeground(Color.decode("#7A8C8D"));
        setForeground(contentColor);
        setFont(new java.awt.Font("sansserif", 0, 13));
        setHint(hint);
        setSelectionColor(new Color(75, 175, 152));
        setDisabledTextColor(Color.decode("#7A8C8D").darker());
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(backgroundColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 5, 5);
        paintIcon(g);
        super.paintComponent(g);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (getText().length() == 0) {
            int h = getHeight();
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            Insets ins = getInsets();
            FontMetrics fm = g.getFontMetrics();
            g.setColor(new Color(200, 200, 200));
            g.drawString(hint, ins.left, h / 2 + fm.getAscent() / 2 - 2);
        }
    }

    private void paintIcon(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        if (prefixIcon != null) {
            Image prefix = ((ImageIcon) prefixIcon).getImage();
            int y = (getHeight() - prefixIcon.getIconHeight()) / 2;
            g2.drawImage(prefix, 10, y, this);
        }
        if (suffixIcon != null) {
            Image suffix = ((ImageIcon) suffixIcon).getImage();
            int y = (getHeight() - suffixIcon.getIconHeight()) / 2;
            g2.drawImage(suffix, getWidth() - suffixIcon.getIconWidth() - 10, y, this);
        }
    }

    private void initBorder() {
        int left = 15;
        int right = 15;
        //  5 is default
        if (prefixIcon != null) {
            //  prefix is left
            left = prefixIcon.getIconWidth() + 15;
        }
        if (suffixIcon != null) {
            //  suffix is right
            right = suffixIcon.getIconWidth() + 15;
        }
        setBorder(javax.swing.BorderFactory.createEmptyBorder(10, left, 10, right));
    }

}
