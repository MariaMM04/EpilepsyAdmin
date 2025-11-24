package ui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
/**
 * Custom JComboBox with themed colors, hover styling, and custom cell rendering.
 * Supports optional hint text and adapts disabled state styling.
 *
 * @param <E> type of elements stored in the combo box
 *
 * @author MamenCortes
 */
public class MyComboBox<E> extends JComboBox<E> {

    private static final long serialVersionUID = 1696707481994733631L;
    private Color backgroundColor = new Color(230, 245, 241);
    private boolean mouseOver;
    private String hint = "";
    /**
     * Creates a new empty styled combo box.
     */
    public MyComboBox() {
        installUI();
    }
    /**
     * Returns whether the mouse is currently over the combo box.
     *
     * @return true if hovered, false otherwise
     */
    public boolean isMouseOver() {
        return mouseOver;
    }
    /**
     * Sets whether the mouse is hovering over the component.
     *
     * @param mouseOver true if the mouse is over the combo box
     */
    public void setMouseOver(boolean mouseOver) {
        this.mouseOver = mouseOver;
    }

    @Override
    public void updateUI() {
        super.updateUI();
        installUI();
    }

    private void installUI() {
        setUI(new StyledComboBoxUI(this));
        setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> jlist, Object o, int i, boolean bln, boolean bln1) {
                Component com = super.getListCellRendererComponent(jlist, o, i, bln, bln1);
                setBorder(new EmptyBorder(5, 5, 5, 5));
                if (bln) {
                    com.setBackground(new Color(240, 240, 240));
                    com.setBackground(backgroundColor.darker());

                }
                return com;
            }
        });
    }

    @Override
    public void setEnabled(boolean b) {
        super.setEnabled(b);
        setEditable(true);
        ComboBoxEditor editor = this.getEditor();
        JTextField etf = (JTextField) editor.getEditorComponent();
        etf.setDisabledTextColor(getForeground().darker());
        etf.setBackground(getBackground());
        // editor.setItem(format(obj));
    }


}


