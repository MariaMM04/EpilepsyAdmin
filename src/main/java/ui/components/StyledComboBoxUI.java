package ui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class StyledComboBoxUI extends BasicComboBoxUI {

    private float location;
    private boolean show;
    private MyComboBox combo;
    private Color backgroundColor = new Color(230, 245, 241);
    private Color foreGroundColor = Color.decode("#7A8C8D");

    public StyledComboBoxUI(MyComboBox combo) {
        this.combo = combo;
        combo.setBackground(backgroundColor);
        combo.setForeground(foreGroundColor.darker());
        combo.setBorder(new EmptyBorder(15, 3, 5, 3));
        combo.setFont(new Font("sansserif", 0, 13));

        combo.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent me) {
                combo.setMouseOver(true);

            }

            @Override
            public void mouseExited(MouseEvent me) {
                combo.setMouseOver(false);

            }
        });

    }


    @Override
    public void paintCurrentValueBackground(Graphics grphcs, Rectangle rctngl, boolean bln) {

    }

    @Override
    protected ComboPopup createPopup() {
        BasicComboPopup pop = new BasicComboPopup(comboBox) {
            @Override
            protected JScrollPane createScroller() {
                list.setFixedCellHeight(30);

                JScrollPane scroll = new JScrollPane(list);
                //scroll.setBackground(Color.WHITE);

                JScrollBar sb = new JScrollBar();
                sb.setUnitIncrement(30);
                sb.setForeground(new Color(180, 180, 180));
                scroll.setVerticalScrollBar(sb);
                return scroll;
            }
        };
        pop.setBorder(new LineBorder(new Color(200, 200, 200), 1));
        return pop;
    }


    @Override
    public void paint(Graphics grphcs, JComponent jc) {
        super.paint(grphcs, jc);
        Graphics2D g2 = (Graphics2D) grphcs;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        int width = combo.getWidth();
        int height = combo.getHeight();
        if (combo.isMouseOver()) {
            g2.setColor(combo.getBackground().darker());
        } else {
            g2.setColor(new Color(150, 150, 150));

        }
        g2.fillRect(2, height -1, width - 4, 1);
        g2.dispose();
    }




}
