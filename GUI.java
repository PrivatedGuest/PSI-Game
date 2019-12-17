import java.awt.*;
import jade.core.AID;
import java.awt.event.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import javax.security.auth.Refreshable;
import javax.swing.*;


/**
 * Graphical User Interface (GUI) example
 *
 * @author  Juan C. Burguillo Rial
 * @version 1.0
 */

//System.setout

/**
 * This class creates a graphical user interface with menus, dialogs and labels.
 *
 * @author  Juan C. Burguillo Rial
 * @version 1.0
 */

class GuiOutputStream extends OutputStream{
    JTextArea cli;
    public GuiOutputStream(JTextArea cli){
        this.cli = cli;
    }
    @Override
    public void write(int data)throws IOException{
        cli.append(new String(new byte[]{(byte) data}));
    }
}

class GUI extends JFrame implements ActionListener, Runnable
{

    final String autors = "Skeleton:Juan C. Burguillo Rial\nChanges:Lionel Salgado Rigueira";

    private MainAgent mainAgent;
    private boolean bProcessExit = false;
    private boolean bProcessWait = false;
    private boolean bProcessReboot = false;
    private Thread oProcess; // Object to manage the thread
    private MyDialog oDl;
    private configuracion config;
    private int locatex = 650;
    private int locatey = 250;
    private int alto;
    private int ancho;
    private int totalpartidas = 0;
    private JTextArea upleft;
    private int nplayers;
    private int ngames;
    private GuiOutputStream cli;
    private boolean verbose;

    /**
     * This is the GUI constructor.
     *
     */
    public configuracion getConfig(){
        return this.config;
    }

    
    GUI(MainAgent agent) {
        super ("GUI"); // Calling the constructor from the parent class

        System.out.println("Inicializada a UI");
        setTitle("GUI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setVisible(true);

        mainAgent = agent;
        config= new configuracion();

        this.alto = Integer.parseInt(config.get("window_height"));
        this.ancho = Integer.parseInt(config.get("window_width"));
        this.verbose = Boolean.parseBoolean(config.get("verbose"));

        setBackground (Color.blue);
        setForeground (Color.red);

        MenuBar oMB = new MenuBar();                             // The menu bar
        MenuItem oMI;                                       // Including the MenuItem in this menu

        Menu oMenu = new Menu("Edit");                           // A Menu in the menu bar
        oMI = new MenuItem ("Reset players", new MenuShortcut('R'));     // Shortcuts are hot keys for executing actions
        oMI.addActionListener (this);
        oMenu.add(oMI);
        oMenu.add(new MenuItem("-"));                       // Including the MenuItem in this menu

        oMI = new MenuItem ("Remove player", new MenuShortcut('B'));     // Shortcuts are hot keys for executing actions
        oMI.addActionListener (this);
        oMenu.add(oMI);
        oMenu.add(new MenuItem("-"));

        oMB.add(oMenu);

        oMenu = new Menu("Window");
        oMI = new MenuItem ("Verbose");     // Shortcuts are hot keys for executing actions
        oMI.addActionListener (this);
        oMenu.add(oMI);
        oMenu.add(new MenuItem("-"));

        oMI = new MenuItem ("Width");     // Shortcuts are hot keys for executing actions
        oMI.addActionListener (this);
        oMenu.add(oMI);
        oMenu.add(new MenuItem("-"));

        oMI = new MenuItem ("Height");     // Shortcuts are hot keys for executing actions
        oMI.addActionListener (this);
        oMenu.add(oMI);
        oMenu.add(new MenuItem("-"));

        oMB.add(oMenu);

        oMenu = new Menu("Run");                           // A Menu in the menu bar                                      // Including the MenuItem in this menu
        oMI = new MenuItem ("New", new MenuShortcut('E'));     // Shortcuts are hot keys for executing actions
        oMI.addActionListener (this);
        oMenu.add(oMI);
        oMenu.add(new MenuItem("-"));

        oMI = new MenuItem ("Stop", new MenuShortcut('P'));     // Shortcuts are hot keys for executing actions
        oMI.addActionListener (this);
        oMenu.add(oMI);
        oMenu.add(new MenuItem("-"));

        oMI = new MenuItem ("Continue", new MenuShortcut('C'));     // Shortcuts are hot keys for executing actions
        oMI.addActionListener (this);
        oMenu.add(oMI);
        oMenu.add(new MenuItem("-"));

        oMI = new MenuItem ("Number of games", new MenuShortcut('N'));     // Shortcuts are hot keys for executing actions
        oMI.addActionListener (this);
        oMenu.add(oMI);
        oMenu.add(new MenuItem("-"));

        oMI = new MenuItem ("Change Nº Players", new MenuShortcut('G'));     // Shortcuts are hot keys for executing actions
        oMI.addActionListener (this);
        oMenu.add(oMI);
        oMenu.add(new MenuItem("-"));
         
        oMI = new MenuItem ("Exit", new MenuShortcut('X'));
        oMI.addActionListener (this);
        oMenu.add(oMI);
        
        oMB.add (oMenu);                                         // Including this menu in the MenuBar

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

        this.ngames = Integer.parseInt(config.get("games"));
        this.nplayers = Integer.parseInt(config.get("players"));
        //Creamos e inicializamos as tres posicións da pantalla principal

    }

    public JSplitPane setupplayers(ArrayList<AID> playerAgents){

        JSplitPane horizontaldivision = new JSplitPane();
        horizontaldivision.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        horizontaldivision.setDividerSize(10);  
        horizontaldivision.setDividerLocation( ancho/3 );

        int k = 0;
        Label toappend = new Label ("Rondas ", Label.CENTER);
        toappend.setFont(new Font("ola",11,28));
        add(toappend);

        for(k=0;k<nplayers;k++){
            toappend = new Label ("Xogador "+k, Label.CENTER);
            toappend.setFont(new Font("ola",11,28));
            add(toappend );
        }
        int i = 0;
        String[] tojtablehead = new String[playerAgents.size()+1];
        Object [][] tojtable= new String[ngames][playerAgents.size()+1];
        k=0;
        for(k=0;k<playerAgents.size()+1;k++){
            if(k==0){
                tojtable[0][k] = "RONDAS";
                tojtablehead[k] = "RONDAS";
            }else{
                tojtable[0][k] = playerAgents.get(k-1).getLocalName();
                tojtablehead[k] = playerAgents.get(k-1).getLocalName();
            }
        }

        for(i=1;i<ngames;i++){
            k=0;
            tojtable[i][k] = "Ronda " + i;
            for(k=1;k<playerAgents.size()+1;k++){
                tojtable[i][k] = " ";
            }
        }

        JSplitPane verticaldivision = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        verticaldivision.setDividerSize(10);
        verticaldivision.setOneTouchExpandable(true);
        verticaldivision.setDividerLocation( alto/2);


        JTable GameTable = new JTable(tojtable,tojtablehead);
        
        GameTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);   
        GameTable.setRowHeight(ancho*2/3/(ngames+1));

        JScrollPane scrollgame = new JScrollPane (GameTable,
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        //Creamos a pantalla da esquerda

        this.upleft = new JTextArea("\nPlayersParticipating:"+nplayers+"\n\nThere is no parameter values at the moment\n\nGames Played:"+totalpartidas+"\n\nNo stadistic players and the moment");
        this.upleft.setFont(new Font("def",2,20));

        horizontaldivision.setLeftComponent(upleft);
        horizontaldivision.setRightComponent(scrollgame);


        verticaldivision.setTopComponent(horizontaldivision);

        JTextArea cliprueba = new JTextArea();
        cli = new GuiOutputStream(cliprueba);
        verticaldivision.setBottomComponent(new JScrollPane(cliprueba));
        System.setOut(new PrintStream(cli,true));
        getContentPane().add(verticaldivision, java.awt.BorderLayout.CENTER);
        setSize (new Dimension(alto,ancho));     // Window size
        setLocation (new Point (locatex, locatey));   // Window position in the screen
        setVisible (true);                    // Let's make the GUI appear in the screen

        return horizontaldivision;
    }

    /*
     * This method recibes and process events related with this class.
     *
     * @param evt In this parameter we receive the event that has been generated.
     */


    public void actionPerformed (ActionEvent evt) {

        if("Remove player".equals (evt.getActionCommand())){
            oDl = new MyDialog (this,"Currentasdasd", true, config.get("players"),"Remove Player","Cancel");
        }
        if ("Change Nº Players".equals (evt.getActionCommand())){
            oDl = new MyDialog (this, "Current Players", true, config.get("players"),"Change Nº players","Cancel");
        }
        else if ("Number of games".equals (evt.getActionCommand())){
            oDl = new MyDialog (this, "Current Games", true, config.get("games"),"Change Nº games","Cancel");
        }
        else if ("New".equals (evt.getActionCommand())){
            vStartThread ();
        }
        else if( "Stop".equals (evt.getActionCommand())){
            vStopThread ();
        }
        else if( "Continue".equals (evt.getActionCommand())){
            this.mainAgent.mandarmensage("OLAAAAA");
            vContinueThread ();
        }else if ("Verbose".equals(evt.getActionCommand())){
            oDl= new MyDialog (this, "Verbose", true, "Activade/Desactivate","Activate Verbose","Desactivate Verbose");
        }else if ("Width".equals(evt.getActionCommand())){
            oDl = new MyDialog (this, "Current Width", true, config.get("window_width"),"Change Width","Cancel");
        }else if ("Height".equals(evt.getActionCommand())){
            oDl = new MyDialog (this, "Current Height", true, config.get("window_height"),"Change Height","Cancel");
        }
        
        else if ("Exit".equals (evt.getActionCommand())) {
            bProcessExit = true;
            dispose();        
            System.exit(0);
        }
    }

    /*
     * This method starts a thread
     */
    private void vStartThread () {
        bProcessReboot = true;
        bProcessWait = false;
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
        bProcessWait = true;
    }

    /**
     * This method continue a thread
     */
    private void vContinueThread () {
        if (oProcess != null){
            bProcessWait = false;
        }       
    }

    /**
     * This method contains the code to be executed in parallel.
     */
    public void run() {
        int i=0;
        while (true) {
            upleft.setText("\nPlayersParticipating:"+this.nplayers+"\n\nThere is no parameter values at the moment\n\nGames Played:"+this.totalpartidas+"\n\nNo stadistic players and the moment");
        
            try {
                i++;
                if(verbose){
                    System.out.println("Working iteration: " + i);
                }
                if(i == ngames)bProcessWait=true;
                Thread.sleep(1000);
                if(bProcessReboot){
                    i=0;
                    this.totalpartidas +=1;
                    bProcessReboot = false;
                    bProcessWait = false;
                }
                while(bProcessWait == true){
                    if(verbose){
                        System.out.println("Sistema Pausado");
                    }
                    
                    if(i== ngames && bProcessReboot == false){
                        System.out.println("Necesita iniciar una nueva partida");
                        bProcessWait = true;
                    }
                };
            }
            catch (InterruptedException oIE) {}
            
            if (bProcessExit) return;
        }
    }

   /*public static void main (String args[]) {
        GUI oGUI= new GUI();
    }*/

    public void print(String parapintar ){
        System.out.println(parapintar);
    }

     public void setPlayersUI(ArrayList<String> players) {
      System.out.print(players);  
     }
}

/**
 * This class produces dialog windows with a text field and two buttons: one to accept and another to cancel.
 *
 * @author  Juan C. Burguillo Rial
 * @version 1.0
 */
class MyDialog extends JDialog implements ActionListener
{
    /**
	 *
	 */
	private static final long serialVersionUID = 1L;
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
    MyDialog (GUI gui, String sDialogName, boolean bBool, String TextField, String ButtonField,String SecondButtonField) {
        super (gui, sDialogName, bBool);

        this.config = gui.getConfig();
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
        oJBut  = new JButton (SecondButtonField);
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

        if("Remove Player".equals(evt.getActionCommand())){
            String sText = oJTF.getText();                     // Getting the present text from the TextField
            this.config.set("players",sText);
            dispose();
        }else if ("Change Nº players".equals (evt.getActionCommand())) {
            String sText = oJTF.getText();                     // Getting the present text from the TextField
            this.config.set("players",sText);
            int iVal = Integer.parseInt (sText);               // Converting such text to several formats
            float fVal = Float.parseFloat (sText);
            double dVal = Double.parseDouble (sText);
            dispose();                                         // Closing the dialog window
        }else if("Change Width".equals(evt.getActionCommand())){
            String sText = oJTF.getText();                     // Getting the present text from the TextField
            this.config.set("window_width",sText);
            dispose();
        }else if("Change Height".equals(evt.getActionCommand())){
            String sText = oJTF.getText();                     // Getting the present text from the TextField
            this.config.set("window_height",sText);
            dispose();
        }else if("Activate Verbose".equals(evt.getActionCommand())){
            this.config.set("verbose","true");
            dispose();   
        }else if("Deactivate Verbose".equals(evt.getActionCommand())){
            this.config.set("verbose","false"); 
            dispose();  
        }
        else if ("Cancel".equals (evt.getActionCommand())){
            dispose();
        }else{
            System.out.print("ESTA FUNCIONANDO MAL ESO");
        }
            
    }   


} // from MyDialog class
