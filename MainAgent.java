import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
 
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

public class MainAgent extends Agent {

    private GUI gui;
    private ArrayList<AID> playerAgents = new ArrayList<AID>();
    private ArrayList<AID> playerTot = new ArrayList<AID>();
    private ArrayList<String> playerNames = new ArrayList<String>();
    private GameParametersStruct parameters = new GameParametersStruct();
    private int currentround = 1;
    private int currentplay = 1;
    private int currentgeneration = 1;
    private boolean verbose;
    private int endowment = 40;
    private int nrounds = 10;
    private int ngenerations = 50;
    private float pd = 0.8f;
    private int nruns = 100;
    private boolean canplay = true;
    //0 vai ser os cartos que lle quedan
    //1 vai ser os cartos acumulados
    public int[][] datos; 
    public int[] ganadores;
    private int puntosestejuego=0;

    public int getngenerations(){
        return this.ngenerations;
    }

    public int getPuntosEsteJuego(){
        return puntosestejuego;
    }

    public void setPuntosEsteJuego(int X){
        this.puntosestejuego = X;
    }

    public void setCurrentGeneration(int X){
        this.currentgeneration = X;
    }

    public int getCurrentGeneration(){
        return this.currentgeneration;
    }

    public boolean getCanPlay(){
        return this.canplay;
    }

    public void setCanPlay(boolean X){
        this.canplay = X;
    }

    public int getCurrentPlay(){
        return this.currentplay;
    }

    public void setCurrentPlay(int X){
        this.currentplay = X;
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

        while(this.canplay == false){
            try{
                Thread.sleep(1000);
                System.out.println("ESPERANDO");
            }catch(Exception e){
                System.out.println("FALLO NO PLAYROUND DO MAIN");
            }
        }

        this.setCurrentPlay(1);
        this.continueGame();

    }

    public void continueGame(){
    
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        for(int k=0;k<playerAgents.size();k++){
            msg.addReceiver(new AID(this.playerAgents.get(k).getLocalName(), AID.ISLOCALNAME));
            datos[k][0] = this.endowment;
        }
        this.setPuntosEsteJuego(0);
        this.setCurrentRound(1);
        this.getGui().setTotalPartidas(this.getGui().getTotalPartidas()+1);
        msg.setLanguage("English");
        msg.setOntology("Play");
        msg.setContent("NewGame");
        send(msg);
        msg.setContent("Actions");
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
                //playerTot.add(result[i].getName());
                playerAgents.add(result[i].getName());
            }
            datos = new int[playerAgents.size()][2];
            ganadores = new int [playerAgents.size()];
        } catch (FIPAException fe) {
            System.out.println(fe.getMessage());
        }
        for(int i =0;i<playerAgents.size();i++){
            datos[i][0] = this.endowment;
            datos[i][1] = 0;
            ganadores[i] = 0;
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
            
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            for(int k=0;k<playerAgents.size();k++){
                msg.addReceiver(new AID(this.agent.playerAgents.get(k).getLocalName(), AID.ISLOCALNAME));
            }
            msg.setLanguage("English");
            msg.setOntology("Play");
            msg.setContent("Action");
            send(msg);
        }

        public void salvaronse(){
            //Ganou o que teña mais cartos
            for(int i =0;i<playerAgents.size();i++){
                datos[i][1] += datos[i][0];
            }
            System.out.println("ESTE XOGADOR LEVA ACUMULADOS"+ String.valueOf(datos[0][1]));         
        
        }

        public void morreron(){System.out.println("DESASTRE!!!PERDERON TODOS");}

        public void  newGeneration(int ganador){

            ganadores[ganador] += 1;

            for(int i =0;i<playerAgents.size();i++){    
                datos[i][1] = 0;
                System.out.println("GANADOOOOOR"+ ganadores[i]);
            }

            int[] copiaganadores  = ganadores.clone();
            
            Arrays.sort(copiaganadores);
            
            int index1 = Arrays.stream(ganadores).boxed().collect(Collectors.toList()).indexOf(copiaganadores[ (playerAgents.size()-1) ]);
            int index2 = Arrays.stream(ganadores).boxed().collect(Collectors.toList()).indexOf(copiaganadores[ (playerAgents.size()-2) ]);
            int index3 = Arrays.stream(ganadores).boxed().collect(Collectors.toList()).indexOf(copiaganadores[ (playerAgents.size()-3) ]);
            
            if(index1 == index2){ //Buscamos o siguiente
                int valorpelea = ganadores[index1];
                for(int i = index1;i<playerAgents.size();i++){
                    if(ganadores[i]==valorpelea)index2=i;
                }
            }
            if(index1 == index3){ //Buscamos o siguiente
                int valorpelea = ganadores[index1];
                for(int i = index1;i<playerAgents.size();i++){
                    if(ganadores[i]==valorpelea)index3=i;
                }
            }
            if(index2 == index3){ //Buscamos o siguiente
                int valorpelea = ganadores[index2];
                for(int i = index2;i<playerAgents.size();i++){
                    if(ganadores[i]==valorpelea)index3=i;
                }
            }



            this.agent.gui.setRanking("Player 1:"+playerAgents.get(index1).getLocalName()+"<br>Player 2:"+playerAgents.get(index2).getLocalName()+
                "<br>Player 3:"+playerAgents.get(index3).getLocalName());
            

            if (this.agent.getCurrentGeneration() != this.agent.getngenerations()){
                this.agent.gui.setTotalPartidas(this.agent.getCurrentGeneration());
                this.agent.setCurrentGeneration(this.agent.getCurrentGeneration()+1);
                this.agent.gui.refreshJLabel();
                this.agent.startGame();
            }

        }

        public int calcularganador(){
            int maior = 0;
            int index = 0;
            for(int i =0;i<playerAgents.size();i++){
                if(datos[i][1]>maior){
                    maior = datos[i][1];
                    index = i;
                }
            }
            return index;
        }

        public void refreshpuntuation(){
            
            int random =(int) (Math.random()*100);
            if( (this.agent.getPuntosEsteJuego() >=  this.agent.endowment*this.agent.getPlayerAgents().size()/2) || random  >80  ){
                this.salvaronse();
            }else{
                this.morreron();
            }

        }

        @Override
        public void action() {
            System.out.println("A INTERFAZ APARECE COA CONSOLA MAXIMIZADA POR DEFECTO");
            while(true){
                ACLMessage msg = myAgent.receive();
                if(msg!=null){
                    if(msg.getOntology().equals("Verbose")){
                        this.getAgent().getGui().print(true,msg.getContent());
                    }else{
                        switch(msg.getContent().substring(0,6).toLowerCase()){
                            case "action":
                                this.setXogarConTodos(this.getXogarConTodos()+1);
                                //Para pintar na tablita
                                this.getAgent().getGui().agentResponse(msg.getContent().substring(7),msg.getSender().getLocalName(),
                                    this.agent.getCurrentRound());
                                //Gardamos todo
                                datos[this.agent.getPlayerAgents().indexOf(msg.getSender())][0] -= Integer.parseInt(msg.getContent().substring(7));
                                this.agent.setPuntosEsteJuego(this.agent.getPuntosEsteJuego()+ Integer.parseInt(msg.getContent().substring(7)));

                                if(this.getXogarConTodos()==this.getAgent().getPlayerAgents().size() ){
                                    //Chegaron todas as mensaxes dos players
                                    this.setXogarConTodos(0);
                                    this.getAgent().getGui().print(true,"Nova ronda,xogadores sincronizados");
                                    //Agora vamos enviarlle as xogadas os participantes
                                    String resultado = "Results#";
                                    for(int i =0;i<playerAgents.size();i++){
                                        resultado += String.valueOf(datos[i][0])+","; 
                                        if(this.agent.playerAgents.get(i).getLocalName().equals(this.agent.getLocalName()) ){
                                        }else{
                                            msg.addReceiver(new AID(this.agent.playerAgents.get(i).getLocalName(), AID.ISLOCALNAME));
                                        }
                                    }
                                    msg.setLanguage("English");
                                    msg.setOntology("Finish_him");
                                    msg.setContent(resultado);
                                    send(msg);
                                        
                                    if(this.agent.getCurrentRound()!=this.agent.nrounds){
                                        //Se non e a ultima ronda xogamos unha
                                        this.agent.setCurrentRound(this.agent.getCurrentRound()+1);
                                        this.playround();
                                    }else{
                                        //Se e a ultima ronda xogamos outra partida
                                        this.refreshpuntuation();
                                        if(this.agent.getCurrentPlay()==this.agent.getnruns()){
                                            //A ultima ronda do ultimo juego
                                            
                                            for(int i =0;i<playerAgents.size();i++){
                                                System.out.println(playerAgents.get(i).getLocalName()+"---"+datos[i][1]);
                                            }
                                            msg.setLanguage("English");
                                            msg.setOntology("Finish_him");
                                            msg.setContent("GameOver");
                                            send(msg);
                                            this.newGeneration(this.calcularganador());
                                        }else{
                                            this.agent.setCurrentPlay(this.agent.getCurrentPlay()+1);
                                            this.agent.continueGame();
                                        }
                                    }
                                }
                                break;
                                
                            case "startu":
                                this.setXogarConTodos(this.getXogarConTodos()+1);
                                if(this.getXogarConTodos()==this.agent.getPlayerAgents().size() ){
                                    this.getAgent().getGui().print(true,"Syncronizacion completada");
                                    this.setXogarConTodos(0);
                                    this.playround();
                                }
                                break;
                            default:
                                int k = 0;
                                //this.getAgent().getGui().print(false,"Agente incompatible con interfaz:::"+msg.getContent());
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
