package ui.components;

import net.miginfocom.swing.MigLayout;
import ui.windows.Application;

import javax.swing.*;
import java.util.ArrayList;

/**
 * Dynamic grid-based menu panel that arranges buttons into rows and columns
 * depending on the number of buttons provided.
 *
 *  @author MamenCortes
 */
public class PanelMenu extends JPanel {

    private static final long serialVersionUID = 1L;
    private ArrayList<JButton> buttons;
    private Integer numButtons;
    private String stringRows = "[][]";
    private Integer numRows = 2;
    private String stringColumns = "[][][]";
    private Integer numColumns = 3;

    /**
     * Creates a menu panel that arranges the given buttons into a responsive grid layout.
     *
     * @param buttons list of buttons to display in the menu
     */
    public PanelMenu(ArrayList<JButton> buttons) {
        //this.buttons = buttons;
        this.numButtons = buttons.size();
        this.buttons = buttons;

        setBackground(Application.light_purple);
        setOpaque(true);

        setLayout();
        initComponents();

    }
    /**
     * Default constructor creating an empty menu panel.
     * Buttons can be added later using {@link #setButtons(ArrayList)}.
     */
    public PanelMenu() {
        this.buttons = new ArrayList<JButton>();
        this.numButtons = buttons.size();

    }
    /**
     * Returns the list of menu buttons.
     *
     * @return list of buttons
     */
    public ArrayList<JButton> getButtons() {
        return buttons;
    }
    /**
     * Replaces the menu buttons and reconstructs the layout dynamically.
     *
     * @param buttons list of new buttons
     */
    public void setButtons(ArrayList<JButton> buttons) {
        this.buttons = buttons;
        setLayout();
        initComponents();
    }
    private void setLayout() {


        if(numButtons%3 == 0) {
            for(int i = 0; i<numButtons/3; i++) {
                stringRows = stringRows+"[]";
                numRows++;
            }
            stringColumns = stringColumns + "[][]";
            numColumns = numColumns +2;

        }else if(numButtons%3 == 1) {
            for(int i = 0; i<1+numButtons/3; i++) {
                stringRows = stringRows+"[]";
                numRows++;
            }
            if(numButtons/3 != 0) {
                stringColumns = stringColumns + "[][]";
                numColumns = numColumns + 2;
            }
        } else {
            for(int i = 0; i<numButtons/3; i++) {
                stringRows = stringRows+"[]";
                numRows++;
            }

            if(numButtons/3 != 0) {
                stringColumns = stringColumns + "[][]";
                numColumns = numColumns + 2;
                stringRows = stringRows+"[]";
                numRows++;
            }else {
                stringRows = stringRows + "[]";
                stringColumns = stringColumns + "[]";
                numRows++;
                numColumns++;
            }
        }
        this.setLayout(new MigLayout("fill, inset 0, gap 10, wrap", stringColumns, stringRows));

    }

    private void initComponents() {


        int buttonOut = 0;
        if(numButtons%3 == 0) {
            for(int i = 0; i<numRows-2; i++) {
                for(int j = 0; j<3; j++) {
                    this.add(buttons.get(buttonOut), "cell "+(j+1)+" "+(i+1)+", grow");
                    buttonOut++;
                    //System.out.println(buttons.get(0).getName()+" in Column: "+(j+1)+" in Row: "+(i+1));
                }
            }
        }else if(numButtons%3 == 1) {
            for(int i = 0; i<numRows-2; i++) {
                int j = 0;
                if(buttons.get(buttonOut) == buttons.get(numButtons-1)){
                    this.add(buttons.get(buttonOut), "cell "+(j+1)+" "+(i+1)+", span "+(numColumns-2)+", split 3, center, grow");
                    buttonOut++;
                }else if(buttonOut < numButtons-1) {
                    for(j = 0; j<3; j++) {

                        this.add(buttons.get(buttonOut), "cell "+(j+1)+" "+(i+1)+", grow");
                        buttonOut++;
                        //System.out.println(buttons.get(buttonOut).getName()+" in Column: "+(j+1)+" in Row: "+(i+1));
                    }
                }
            }

        }else {
            for(int i = 0; i<numRows-2; i++) {
                int j = 0;
                if(buttons.get(buttonOut) == buttons.get(numButtons-2)){
                    this.add(buttons.get(buttonOut), "cell "+(j+1)+" "+(i+1)+", span "+(numColumns-2)+", split 3, center, grow");
                    buttonOut++;

                    this.add(buttons.get(buttonOut), "grow, gap 0");
                    buttonOut++;
                }else if(buttonOut < numButtons-3) {
                    for(j = 0; j<3; j++) {
                        this.add(buttons.get(buttonOut), "cell "+(j+1)+" "+(i+1)+", grow");
                        buttonOut++;
                        //System.out.println(buttons.get(buttonOut).getName()+" in Column: "+(j+1)+" in Row: "+(i+1));
                    }
                }
            }
        }
    }
}
