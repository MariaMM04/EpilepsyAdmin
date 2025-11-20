package ui.components;

import net.miginfocom.swing.MigLayout;
import ui.windows.Application;

import javax.swing.*;
import java.awt.*;
/**
 * A dialog panel displaying a question and two action buttons.
 * It arranges the components using MigLayout and applies custom colors.
 */
public class QuestionDialog extends JPanel {
    /**
     * Creates a new QuestionDialog containing the provided question text
     * along with OK and Cancel buttons.
     *
     * @param questionText the text of the question to display
     * @param okbutton the confirmation button
     * @param cancelbutton the cancellation button
     */
    public QuestionDialog(String questionText, MyButton okbutton, MyButton cancelbutton) {
        this.setLayout(new MigLayout("wrap 2, fill, inset 10, gap 5", "[50%][50%]", "push[][][][][]push"));
        this.setBackground(Color.white);
        this.setPreferredSize(new Dimension(400, 300));

        JTextArea label = new JTextArea(questionText);
        label.setEditable(false);
        label.setLineWrap(true);
        label.setWrapStyleWord(true);
        label.setFont(new Font("sansserif", 1, 20));
        label.setForeground(Application.dark_purple);
        this.add(label, "center, span 2, grow");

        if(okbutton.getText() == ""){okbutton.setText("SAVE");}
        okbutton.setBackground(Application.turquoise);
        okbutton.setForeground(new Color(250, 250, 250));

        if(cancelbutton.getText() == ""){okbutton.setText("CANCEL");}
        cancelbutton.setBackground(Application.turquoise);
        cancelbutton.setForeground(new Color(250, 250, 250));

        this.add(okbutton, "grow, left");
        this.add(cancelbutton, "grow, right");

    }
}
