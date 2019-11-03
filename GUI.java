import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


/**
 * Graphical User Interface (GUI) example
 *
 * @author  Juan C. Burguillo Rial
 * @version 1.0
 */



/**
 * This class creates a graphical user interface with menus, dialogs and labels.
 *
 * @author  Juan C. Burguillo Rial
 * @version 1.0
 */
class GUI extends JFrame implements ActionListener, Runnable
{

    final String autors = "Skeleton:Juan C. Burguillo Rial\nChanges:Lionel Salgado Rigueira";

    private boolean bProcessExit = false;
    private Thread oProcess; // Object to manage the thread
    private MyDialog oDl;
    private configuracion config;
    private int locatex = 650;
    private int locatey = 250;
    /**
     * This is the GUI constructor.
     *
     */
    GUI() {
        super (" GUI"); // Calling the constructor from the parent class

        config= new configuracion();

        setBackground (Color.blue);
        setForeground (Color.red);

        MenuBar oMB = new MenuBar();                             // The menu bar
        Menu oMenu = new Menu("Main");                           // A Menu in the menu bar
        MenuItem oMI;                                       // Including the MenuItem in this menu
        oMI = new MenuItem ("Start", new MenuShortcut('O'));     // Shortcuts are hot keys for executing actions
        oMI.addActionListener (this);
        oMenu.add(oMI);
        oMenu.add(new MenuItem("-"));

        oMI = new MenuItem ("Stop", new MenuShortcut('P'));     // Shortcuts are hot keys for executing actions
        oMI.addActionListener (this);
        oMenu.add(oMI);
        oMenu.add(new MenuItem("-"));
         
        oMI = new MenuItem ("Exit", new MenuShortcut('X'));
        oMI.addActionListener (this);
        oMenu.add(oMI);
        oMB.add (oMenu);                                         // Including this menu in the MenuBar

        oMenu = new Menu("Modify") ;
        oMI = new MenuItem ("Nº Players", new MenuShortcut('C'));
        oMI.addActionListener (this);
        oMenu.add(oMI);
        oMB.add (oMenu);
        
        oMenu = new Menu("Help");
        oMI = new MenuItem ("About");
        oMI.addActionListener (new ActionListener(){

            public void actionPerformed(ActionEvent e){
                JOptionPane.showMessageDialog(null, autors);
            }

        });
        
        oMI.addActionListener (this);
        oMenu.add(oMI);
        oMB.add (oMenu);
        oMB.setHelpMenu (oMenu);                                 // Helps menus appear in some OSs in the right side

        setMenuBar(oMB);

        setLayout(new GridLayout(1,1));
        add (new Label ("This is a label !!!", Label.CENTER));

        setSize (new Dimension(750,500));     // Window size
        setLocation (new Point (locatex, locatey));   // Window position in the screen
        setVisible (true);                    // Let's make the GUI appear in the screen
    }

    /**
     * This method recibes and process events related with this class.
     *
     * @param evt In this parameter we receive the event that has been generated.
     */
    public void actionPerformed (ActionEvent evt) {
        if ("Nº Players".equals (evt.getActionCommand())){
            oDl = new MyDialog (this.config,this, "Current Players", true, config.getplayers(),"Change Nº players");
        }
        else if ("Start".equals (evt.getActionCommand()))
            vStartThread ();

        else if( "Stop".equals (evt.getActionCommand())){
            vStopThread ();
        }
        
        else if ("Exit".equals (evt.getActionCommand())) {
            dispose();        
            System.exit(0);
        }
    }



    /*
     * This method starts a thread
     */
    private void vStartThread () {
        if (oProcess == null) {
            oProcess = new Thread (this);
            oProcess.start();
            bProcessExit = false;
        }
    }


    /**
     * This method stops a thread
     */
    private void vStopThread () {
        if (oProcess != null)
            bProcessExit = true;
    }


    /**
     * This method contains the code to be executed in parallel.
     */
    public void run() {
        int i=0;
        while (true) {

            try {
                i++;
                System.out.println("Working iteration: " + i);
                Thread.sleep(1000);
            }
            catch (InterruptedException oIE) {}

            if (bProcessExit) return;
        }
    }



    public static void main (String args[]) {
        GUI oGUI= new GUI();
    }

} // from the class GUI

/**
 * This class produces dialog windows with a text field and two buttons: one to accept and another to cancel.
 *
 * @author  Juan C. Burguillo Rial
 * @version 1.0
 */
class MyDialog extends JDialog implements ActionListener
{
    private JTextField oJTF;
    private int locatex = 650;
    private int locatey = 250;
    configuracion config;

    /**
     * This is the MyDialog class constructor
     *
     * @param oParent Reference to the object that has created this MyDialog object
     * @param sDialogName Name of this dialog window
     * @param bBool Indicates if this is a modal window (true) or not.
     */
    MyDialog (configuracion config,Frame oParent, String sDialogName, boolean bBool, String TextField, String ButtonField) {
        super (oParent, sDialogName, bBool);

        this.config =config;
        setBackground (Color.red); // Colors
        setForeground (Color.blue);

        setLayout (new GridLayout(2,1));
        
        oJTF = new JTextField (TextField, 30);
        add (oJTF);

        JPanel oJPanel = new JPanel();
        oJPanel.setLayout (new GridLayout(1,2));
        JButton oJBut = new JButton (ButtonField);
        oJBut.addActionListener (this);
        oJPanel.add (oJBut);
        oJBut  = new JButton ("Cancel");
        oJBut.addActionListener (this);
        oJPanel.add (oJBut);
        add (oJPanel);

        setSize (new Dimension(350,150));
        setLocation (new Point (locatex, locatey));
        setVisible (true);
    }



    /**
     * This method recibes and process events related with this class.
     *
     * @param evt In this parameter we receive the event that has been generated.
     */
    public void actionPerformed (ActionEvent evt) {
        if ("Change Nº players".equals (evt.getActionCommand())) {
            String sText = oJTF.getText();                     // Getting the present text from the TextField
            this.config.setplayers(sText);
            int iVal = Integer.parseInt (sText);               // Converting such text to several formats
            float fVal = Float.parseFloat (sText);
            double dVal = Double.parseDouble (sText);
            dispose();                                         // Closing the dialog window
        }

        else if ("Cancel".equals (evt.getActionCommand()))
            dispose();
    }


} // from MyDialog class
