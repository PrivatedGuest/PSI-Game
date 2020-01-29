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
    private ArrayList<Integer> numberPlays = new ArrayList<Integer>();
    private ArrayList<Integer> jugadoresEliminados = new ArrayList<Integer>();
    private ArrayList<String> playerNames = new ArrayList<String>();
    private GameParametersStruct parameters = new GameParametersStruct();
    private int currentround = 1;
    private int currentplay = 1;
    private int currentgeneration = 1;
    private boolean verbose;
    private int endowment = 40;
    private int nrounds = 10;
    private int nplayers = 5;
    private int ngenerations = 50;
    private float pd = 0.8f;
    private String fase;
    private int Ngames = 100;
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

    public String getPlayersDeletedString(){
        String devolver = " ";
        if(jugadoresEliminados.size() == 0){
            return "None";
        }else{
            for(int i=0;i<jugadoresEliminados.size();i++){
                devolver += playerTot.get(jugadoresEliminados.get(i)).getName()+"\n";
            }
        }
        return devolver;
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

    public int getNgames(){
        return this.Ngames;
    }

    public void setNgames(int X){
        this.Ngames=X;
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

        if(this.fase.equals("Warm-Up")){
            playerAgents = getWarmUpNewAgents();
            gui.setupplayers(playerAgents);
        }
        
        while(this.canplay == false){
            try{
                Thread.sleep(2000);
                System.out.println("Espero desbloqueo");
            }catch(Exception e){
                System.out.println("FALLO NO PLAYROUND DO MAIN");
            }
        }

        this.setCurrentPlay(1);
        this.continueGame();

    }

    public void resetstats(){
        
        for(int i =0;i<playerTot.size();i++){
            datos[i][0] = this.endowment;
            datos[i][1] = 0;
            ganadores[i] = 0;
        }try{
            this.gui.setWinner("No last round winner");
            this.gui.setRanking("No ranking at the moment");
            this.gui.refreshJLabel();
        }catch(Exception e){
            System.out.println("E a primeira ronda");
        }
        
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
        /*try{
            Thread.sleep(10);
        }catch(Exception e){
            System.out.println("FALLO NO PLAYROUND DO MAIN");
        }*/
        msg.setContent("Action");
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
                playerTot.add(result[i].getName());
                numberPlays.add(0);
                //playerAgents.add(result[i].getName());
            }
            datos = new int[playerTot.size()][2];
            ganadores = new int [playerTot.size()];
        } catch (FIPAException fe) {
            System.out.println(fe.getMessage());
        }
        for(int i =0;i<playerTot.size();i++){
            datos[i][0] = this.endowment;
            datos[i][1] = 0;
            ganadores[i] = 0;
        }
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        int contador =0;
        for (AID aid : playerTot) {
            playerNames.add(aid.getLocalName());
            ACLMessage msge = new ACLMessage(ACLMessage.INFORM);
            msge.addReceiver(aid);
            msge.setOntology("StartUp");
            msge.setContent("Id#"+contador+"#"+playerTot.size()+","+this.getEndowment()+","+
                        this.getnrounds()+","+this.getPd()+","+this.getNgames());
            send(msge);
            contador++;
        }

        newGame();

        this.newFullGame();
        
        return 0;
    }
    
    //Devolve o valor maximo do array
    public int maximoArray(ArrayList<Integer> x){
        int maximo = -1;
        for(int i=0;i<x.size();i++){
            if(x.get(i) > maximo ){
                maximo = x.get(i);
            }
        }
        return maximo;
    }

    //Devolve o indice onde esta o valor maximo
    public int indiceMaximo(ArrayList<Integer> x){
        int maximo = -1;
        int indice = -1;
        for(int i=0;i<x.size();i++){
            if(x.get(i) > maximo ){
                maximo = x.get(i);
                indice = i;
            }
        }
        return indice;
    }

    public ArrayList<AID> getWarmUpNewAgents(){
        ArrayList<AID> auxagents = new ArrayList<AID>();
        ArrayList<Integer> auxiliar = new ArrayList<Integer>();
        ArrayList<Integer> indexusados = new ArrayList<Integer>();
        auxiliar.add(20000);//200 por poñer algo random,repetimolo 5 veces
        auxiliar.add(20000);
        auxiliar.add(20000);
        auxiliar.add(20000);
        auxiliar.add(20000);
        indexusados.add(20000);
        indexusados.add(20000);
        indexusados.add(20000);
        indexusados.add(20000);
        indexusados.add(20000);
        for(int i=0; i<playerTot.size();i++){
            //Se este xogador xogou menos que o que mais xogou do array,metemolo
            if( numberPlays.get(i) < maximoArray(auxiliar)){ //Para coller os que menos xogaron
                int maxind = indiceMaximo(auxiliar); //O que mais xogou do array
                auxiliar.set(maxind,numberPlays.get(i)); //Escribimos o numero de partidas que xogou
                indexusados.set(maxind,i);//Gardamos o indice do xogador
            }
        }
        //Agora temos os indices dos xogadores,auxiliar solo o necesitabamos para saber quenes eran os 
        //que menos xogaran
        playerAgents.clear();
        for(int i=0;i<indexusados.size();i++){
            auxagents.add( playerTot.get(indexusados.get(i)) );//Engadimos os xogadores que nos interesan
            
            numberPlays.set(indexusados.get(i),numberPlays.get(indexusados.get(i))+1);
        }
        System.out.println(auxagents);
        //Agora temos 
        return auxagents;
    }

    public void removePlayer(String nombre){
        
        for(int i=0;i<playerAgents.size();i++){
            if(playerAgents.get(i).getLocalName().equals(nombre)){
                playerAgents.remove(playerAgents.get(0));
                gui.setupplayers(playerAgents);
            }
        }
    }

    public void newFullGame(){
        this.resetstats();
        jugadoresEliminados.clear();
        this.fase="Warm-Up";
        this.setNgames(50);
        this.gui.setFase(this.fase);
        this.gui.setPlayersRemaining(playerTot.size());
        this.ngenerations = 10;
        //ESTO E SOLO PARA TESTEAR

        this.gui.setTotalPartidas(1);
        this.setCurrentGeneration(1);
        
        this.startGame();
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
        }

        public void morreron(){
            this.agent.gui.print(true,"DESASTRE!!!PERDERON TODOS");
        }


        public void  newGeneration(int ganador){

            if(!this.agent.fase.equals("Warm-Up")){
                //Non nos interesa o dos ganadores nin nada desto se estamos no WarmUp
                ganadores[ganador] += 1;
                this.getAgent().getGui().print(true,"\nNova partida, esta ganou" + this.agent.playerAgents.get(ganador).getLocalName());
                this.agent.gui.setWinner(this.agent.playerAgents.get(ganador).getLocalName()  );

                for(int i =0;i<playerAgents.size();i++){    
                    datos[i][1] = 0;
                }

                int[] copiaganadores  = ganadores.clone();
                
                Arrays.sort(copiaganadores);
                
                int index1 = Arrays.stream(ganadores).boxed().collect(Collectors.toList()).indexOf(copiaganadores[ (playerAgents.size()-1) ]);
                int index2 = Arrays.stream(ganadores).boxed().collect(Collectors.toList()).indexOf(copiaganadores[ (playerAgents.size()-2) ]);
                int index3 = Arrays.stream(ganadores).boxed().collect(Collectors.toList()).indexOf(copiaganadores[ (playerAgents.size()-3) ]);
                
                if(index1 >= playerAgents.size()){index3=playerAgents.size()-1;}
                if(index2 >= playerAgents.size()){index3=playerAgents.size()-1;}
                if(index3 >= playerAgents.size()){index3=playerAgents.size()-1;}
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
                

                this.agent.gui.setRanking("<p style='text-align: center;''><span style='color: #000080;'>Ranking</span></p><br>Player 1:"+playerAgents.get(index1).getLocalName()+"<br>Player 2:"+playerAgents.get(index2).getLocalName()+
                    "<br>Player 3:"+playerAgents.get(index3).getLocalName());
            }

            if (this.agent.getCurrentGeneration() != this.agent.getngenerations()){
                //SE NON ACABAMOS A XERACION SEGUIMOS
                this.agent.setCurrentGeneration(this.agent.getCurrentGeneration()+1);
                this.agent.gui.refreshJLabel();
                this.agent.startGame();
            }else if(this.agent.fase.equals("Warm-Up")){
                //SE ESTABAMOS NO WARM-UP TOCANOS IR O BATTLE ROYALE
                System.out.println("AGORA TOCARIA O BATTLE ROYAL!!!");
                this.agent.fase = "BattleRoyal";
                this.agent.gui.setFase(this.agent.fase);
                this.agent.ngenerations = 20;
                this.agent.setCurrentGeneration(0);
                playerAgents.clear();
                for(int i =0;i<playerTot.size();i++){
                    playerAgents.add((AID)playerTot.get(i).clone());
                }
                gui.setupplayers(playerAgents);
                this.agent.gui.refreshJLabel();
                this.agent.startGame();
            }else if(this.agent.fase.equals("BattleRoyal")){
                //Se estamos aqui e que xa abamos(minimo) a primeira ronda do battle royale
                int cartosminimos = 123123123;
                int cartosmaximos = 0;
                int indicecartosminimos = 0;
                int indicecartosmaximos = 0;
                for(int i =0;i<playerAgents.size();i++){
                    if(ganadores[i] < cartosminimos){
                        cartosminimos = ganadores[i];
                        indicecartosminimos = i;
                    }
                    System.out.print(playerAgents.get(i).getName()+"---> "+ganadores[i]+"\n");
                }
                //EEEEEEEEEEEEEEEEEEEEEE
                //EEEEEEEEEEEEEEEEEEEEEE
                //EEEEEEEEEEEEEEEEEEEEEE
                //EEEEEEEEEEEEEEEEEEEEEE
                //Temos que facer un novo playeragents para modificar o noso antollo
                System.out.println("Vamos a eliminar a "+playerAgents.get(indicecartosminimos).getName());
                try{Thread.sleep(3000);}catch(Exception e){}
                playerAgents.remove(indicecartosminimos);
                jugadoresEliminados.add(indicecartosminimos);
                this.agent.gui.setPlayersRemaining( playerAgents.size()-jugadoresEliminados.size() );
                if( (playerAgents.size()/*-jugadoresEliminados.size()*/) >5){
                    gui.setupplayers(playerAgents);
                    this.agent.gui.refreshJLabel();
                    this.agent.ngenerations = 25;
                    this.agent.setCurrentGeneration(0);
                    this.agent.startGame();
                }else{
                    System.out.println("TOCA A FASE FINAL");
                    this.agent.resetstats();
                    this.agent.fase = "FINAL";
                    this.agent.gui.setFase(this.agent.fase);
                    this.agent.setNgames(500);
                    this.agent.ngenerations = 1;
                    this.agent.setCurrentGeneration(1);
                    this.agent.gui.setupplayers(playerAgents);
                    this.agent.gui.refreshJLabel();
                    this.agent.startGame();
                }   
            }else{
                int cartosmaximos = 0;
                int indicecartosmaximos = 0;
                for(int i =0;i<playerAgents.size();i++){
                    if(ganadores[i] > cartosmaximos ){
                        cartosmaximos = ganadores[1];
                        indicecartosmaximos = i;
                    }
                }
                    System.out.println("GANAD@R DO TORNEO E..>"+playerAgents.get(indicecartosmaximos).getName());
            
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
            //System.out.println("Temos "+this.agent.getPuntosEsteJuego()+"e necesitamos"+this.agent.endowment*this.agent.getPlayerAgents().size()/2);
            int random =(int) (Math.random()*100);
            if( (this.agent.getPuntosEsteJuego() >=  this.agent.endowment*this.agent.getPlayerAgents().size()/2) || random  >=80  ){
                this.salvaronse();
            }else{
                this.morreron();
            }

        }

        @Override
        public void action() {
            System.out.println("###########################################################");
            System.out.println("A INTERFAZ APARECE COA CONSOLA MAXIMIZADA POR DEFECTO");
            System.out.println("###########################################################");
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
                                datos[this.agent.getPlayerAgents().indexOf(msg.getSender())][0] = 4-Integer.parseInt(msg.getContent().substring(7));
                                this.agent.setPuntosEsteJuego(this.agent.getPuntosEsteJuego()+ Integer.parseInt(msg.getContent().substring(7)));

                                if(this.getXogarConTodos()==this.getAgent().getPlayerAgents().size() ){
                                    //Chegaron todas as mensaxes dos players
                                    this.setXogarConTodos(0);
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
                                        msg.setLanguage("English");
                                        msg.setOntology("Finish_him");
                                        msg.setContent("GameOver");
                                        send(msg);
                                        if(this.agent.getCurrentPlay()==this.agent.getNgames()){
                                            //A ultima ronda do ultimo juego
                                            for(int i =0;i<playerAgents.size();i++){
                                                this.agent.getGui().print(false,"["+this.agent.fase+"]" +playerAgents.get(i).getLocalName()+"----->"+datos[i][1]+ "  (puntos)");
                                            }
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
