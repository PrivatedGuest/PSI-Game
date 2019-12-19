import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
 
import java.io.PrintStream;
import java.util.ArrayList;

public class MainAgent extends Agent {

    private GUI gui;
    private ArrayList<AID> playerAgents = new ArrayList<AID>();
    private ArrayList<String> playerNames = new ArrayList<String>();
    private GameParametersStruct parameters = new GameParametersStruct();
    private int currentround = 1;
    private boolean verbose;
    private int endowment =40;
    private int nrounds = 10;
    private float pd = 0.8f;
    private int nruns = 20;
    private boolean canplay = false;
    //1 vai ser o que xogou a ultima ronda
    //2 vai ser os cartos desta ronda
    //3 vai ser o numero de victorias
    public String[][] datos; 

    public boolean getCanPlay(){
        return this.canplay;
    }

    public void setCanPlay(boolean X){
        this.canplay = X;
    }

    public int getnrounds(){
        return nrounds;
    }

    public int getEndowment(){
        return this.endowment;
    }

    public float getPd(){
        return this.pd;
    }

    public int getnruns(){
        return this.nruns;
    }

    public int getCurrentRound(){
        return this.currentround;
    }

    public void setCurrentRound(int X){
        this.currentround = X;
    }

    public ArrayList<AID> getPlayerAgents(){

        return playerAgents;
    }

    @Override
    protected void setup() {
        gui = new GUI(this);
        updatePlayers();    
        gui.print(false,"Agent " + getAID().getName() + " is ready.");
    }
    
    public GUI getGui(){
        return this.gui;
    }

    public void setVerbose(boolean x){
        this.verbose = x;
    }

    public void startGame(){
    
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        for(int k=0;k<playerAgents.size();k++){
            msg.addReceiver(new AID(this.playerAgents.get(k).getLocalName(), AID.ISLOCALNAME));
        }
        
        this.setCurrentRound(1);
        msg.setLanguage("English");
        msg.setOntology("Play");
        msg.setContent("NewGame");
        this.setCanPlay(false);
        send(msg);
        this.getGui().setTotalPartidas(this.getGui().getTotalPartidas()+1);
    }   

    public int updatePlayers() {
        gui.print(false,"Updating player list");
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Player");
        template.addServices(sd);
        try {
            DFAgentDescription[] result = DFService.search(this, template);
            if (result.length > 0) {
                System.out.println("Found " + result.length + " players");
            }
            for (int i = 0; i < result.length; ++i) {
                playerAgents.add(result[i].getName());

            }
            datos = new String[playerAgents.size()][3];
        } catch (FIPAException fe) {
            System.out.println(fe.getMessage());
        }
        for(int i =0;i<playerAgents.size();i++){
            datos[i][0] = "0";
            datos[i][1] = Integer.toString(this.endowment);
            datos[i][2] = "0";
        }
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        int contador =0;
        for (AID aid : playerAgents) {
            playerNames.add(aid.getLocalName());
            ACLMessage msge = new ACLMessage(ACLMessage.INFORM);
            msge.addReceiver(aid);
            msge.setOntology("StartUp");
            msge.setContent("Id#"+contador+"#"+playerAgents.size()+","+this.getEndowment()+","+
                        this.getnrounds()+","+this.getPd()+","+this.getnruns());
            send(msge);
            contador++;
        }    

        gui.setupplayers(playerAgents);

        newGame();
        return 0;
    }

    public int newGame() {
        addBehaviour(new GameManager(this));
        return 0;
    }

    /**
     * In this behavior this agent manages the course of a match during all the
     * rounds.
     */
    private class GameManager extends SimpleBehaviour {

        private MainAgent agent;
        private int xogarcontodos=0;

        public int getXogarConTodos(){
            return this.xogarcontodos;
        }

        public void setXogarConTodos(int x){
            this.xogarcontodos = x;
        }

        
        public GameManager(MainAgent agent){
            this.agent = agent;
        }

        public MainAgent getAgent(){
            return this.agent;
        }

        public void playround(){
            
            while(!this.agent.canplay){
                System.out.print(this.agent.canplay);
                try{
                    Thread.sleep(1000);
                    System.out.println("ESPERANDO");
                }catch(Exception e){
                    System.out.println("FALLO NO PLAYROUND DO MAIN");
                }
            }
            
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            for(int k=0;k<playerAgents.size();k++){
                msg.addReceiver(new AID(this.agent.playerAgents.get(k).getLocalName(), AID.ISLOCALNAME));
            }
            msg.setLanguage("English");
            msg.setOntology("Play");
            msg.setContent("Action");
            send(msg);
        }

        public void salvaronse(){System.out.println("Salvaronse");}
        public void morreron(){System.out.println("Morreron");}

        public void refreshpuntuation(){

            int ptotal =0;
            for(int i =0; i< this.agent.getPlayerAgents().size();i++){
                ptotal +=  Integer.parseInt(datos[i][0]);
            }
            if( (ptotal > this.agent.endowment*this.agent.getPlayerAgents().size()/2) || (int) (Math.random()*100)  >80  ){
                this.salvaronse();
            }else{
                this.morreron();
            }

        }

        @Override
        public void action() {

            while(true){
                ACLMessage msg = myAgent.receive();
                if(msg!=null){
                    if(msg.getOntology().equals("Verbose")){
                        this.getAgent().getGui().print(true,msg.getContent());
                    }else{

                    
                        switch(msg.getContent().substring(0,6).toLowerCase()){
                            case "action":
                                this.setXogarConTodos(this.getXogarConTodos()+1);
                                
                                this.getAgent().getGui().agentResponse(msg.getContent().substring(7),msg.getSender().getLocalName(),
                                    this.agent.getCurrentRound());

                                int indexplayer= this.agent.getPlayerAgents().indexOf(msg.getSender());

                                datos[indexplayer][0] = msg.getContent().substring(7);

                                if(this.getXogarConTodos()==this.getAgent().getPlayerAgents().size() ){
                                    this.setXogarConTodos(0);//cando se execute este é que xa chegou un dos de antes
                                    this.getAgent().getGui().print(true,"Nova ronda,xogadores sincronizados");
                                    //Agora vamos enviarlle as xogadas os participantes
                                    String resultado = "Results#";
                                    for(int i =0;i<playerAgents.size();i++){
                                        resultado += datos[i][0]+",";        
                                        msg.addReceiver(new AID(this.agent.playerAgents.get(i).getLocalName(), AID.ISLOCALNAME));
                                    }
                                    msg.setLanguage("English");
                                    msg.setOntology("Finish_him");
                                    msg.setContent(resultado);
                                    send(msg);

                                    if(this.agent.getCurrentRound()!=this.agent.nrounds){
                                        this.agent.setCurrentRound(this.agent.getCurrentRound()+1);
                                        this.playround();
                                    }else{
                                        for(int i =0;i<playerAgents.size();i++){
                                            msg.addReceiver(new AID(this.agent.playerAgents.get(i).getLocalName(), AID.ISLOCALNAME));
                                        }
                                        this.refreshpuntuation();
                                        msg.setLanguage("English");
                                        msg.setOntology("Finish_him");
                                        msg.setContent("GameOver");
                                        send(msg);
                                    }
                                }
                                break;
                                
                            case "startu":
                                this.setXogarConTodos(this.getXogarConTodos()+1);
                                if(this.getXogarConTodos()==this.agent.getPlayerAgents().size() ){
                                    this.agent.setCanPlay(true);
                                    this.getAgent().getGui().print(true,"Syncronizacion completada");
                                    this.setXogarConTodos(0);
                                    this.playround();
                                }
                                break;
                            default:
                                this.getAgent().getGui().print(false,"Agente incompatible con interfaz"+msg.getContent());
                        }
                    }
                }else{
                    
                    block();
                }
            }
        }

        @Override
        public boolean done() {
            return true;
        }
    }

    public class PlayerInformation {

        AID aid;
        int id;

        public PlayerInformation(AID a, int i) {
            aid = a;
            id = i;
        }

        @Override
        public boolean equals(Object o) {
            return aid.equals(o);
        }
    }

    public class GameParametersStruct {

        int N;
        int S;
        int R;
        int I;
        int P;

        public GameParametersStruct() {
            N = 2;
            S = 4;
            R = 50;
            I = 0;
            P = 10;
        }
    }
}
