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
    private boolean verbose;


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

    public void play(){
    
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        for(int k=0;k<playerAgents.size();k++){
            msg.addReceiver(new AID(this.playerAgents.get(k).getLocalName(), AID.ISLOCALNAME));
        }
        
        msg.setLanguage("English");
        msg.setOntology("Play");
        msg.setContent("XOGA OSTIA");
        send(msg);
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
        } catch (FIPAException fe) {
            System.out.println(fe.getMessage());
        }
        
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        int contador =0;
        for (AID aid : playerAgents) {
            playerNames.add(aid.getLocalName());
            ACLMessage msge = new ACLMessage(ACLMessage.INFORM);
            msge.addReceiver(aid);
            msge.setOntology("StartUp");
            msge.setContent("Id#"+contador+"#"+playerAgents.size()+",40,10,0.8,4");
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
        private int nrondas=1;
        private int xogarcontodos=0;

        public int getnrondas(){
            return this.nrondas;
        }

        public int getXogarConTodos(){
            return this.xogarcontodos;
        }

        public void setXogarConTodos(int x){
            this.xogarcontodos = x;
        }

        public void setnrondas(int X){
            this.nrondas = X;
        }
        
        public GameManager(MainAgent agent){
            this.agent = agent;
        }

        public MainAgent getAgent(){
            return this.agent;
        }
        @Override
        public void action() {

            while(true){
                ACLMessage msg = myAgent.receive();
                if(msg!=null){
                    switch(msg.getOntology()){

                        case "Resultado":
                            
                            System.out.println("Xogador "+msg.getSender().getLocalName()+" na ronda "+this.getnrondas());

                            this.getAgent().getGui().agentResponse(msg.getContent(),
                                                                    msg.getSender().getLocalName(),
                                                                    this.getnrondas());
                            
                            if(this.getXogarConTodos()==this.getAgent().getPlayerAgents().size() ){
                                this.setXogarConTodos(0);
                                this.setnrondas(this.getnrondas()+1);
                            }else{
                                this.setXogarConTodos(this.getXogarConTodos()+1);
                            }
                            break;
                        case "Verbose":
                            this.getAgent().getGui().print(true,"OLAA DENDE O CASE VERBOSE");
                            break;
                        default:
                            this.getAgent().getGui().print(false,"Agente incompatible con interfaz");
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
