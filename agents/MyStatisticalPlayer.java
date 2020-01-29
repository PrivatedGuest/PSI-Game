package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.util.Random;

public class MyStatisticalPlayer extends Agent {

    private AID mainAgent;
    private int myId, opponentId;
    private int N, S, R, I, P;
    private ACLMessage msg;

    // * This method returns the next play {0,1,2,3,4}



 
  
    protected void setup() {
        //Register in the yellow pages as a player
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Player");
        sd.setName("Game");
        dfd.addServices(sd);
        
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        addBehaviour(new Play());
        System.out.println("StatsPlayer " + getAID().getName() + " is ready.");

    }

    protected void takeDown() {
        //Deregister from the yellow pages
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        System.out.println("StatsPlayer " + getAID().getName() + " terminating.");
    }

    private class Play extends CyclicBehaviour {
        Random random = new Random(1000);
        AID mainAgent;
        public MyStatisticalPlayer agent;
        int id;
        int nplayers;
        int endowment;
        int endowmentusado=0;
        int roundavg;
        float pd;
        int numgames;

        int bAllActions = 0;				// At the beginning we did not try all actions
        int train =0;
        int totaltrain = 6;
        int iNumActions = 4; 					//we use it from 0 so we have 5 possible actions
        int iLastAction;					// The last action that has been played by this player
        public int resulttot = 0;
        //We are creating this class in order to be clear with the code, but we can use the index of "iNumTimesActions" as a value
        int [] possibleActions = new int[]{0,1,2,3,4}; //This is hardcoded cause only we knows the bets
        int[] iNumTimesAction = new int [iNumActions];		// Number of times an action has been played
        int[] dtotalPayoffAction = new int [iNumActions];

        /*public Play(RandomAgent X){
            this.agent = X;
        }*/

        @Override
        public void action() {
            while(true){
                try{
                    ACLMessage mensaxe = receive();
                    if(mensaxe!=null){
                        this.mainAgent=mensaxe.getSender();
                        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                        msg.addReceiver(this.mainAgent);
                        switch(mensaxe.getContent().substring(0,3).toLowerCase()){
                            case "id#":
                                String aux[] = mensaxe.getContent().split("#");
                                this.id = Integer.parseInt(aux[1]);
                                String auxcomas[] = aux[2].split(",");
                                this.nplayers = Integer.parseInt(auxcomas[0]);
                                this.endowment = Integer.parseInt(auxcomas[1]);

                                this.roundavg = Integer.parseInt(auxcomas[2]);
                                this.pd = Float.parseFloat(auxcomas[3]);
                                this.numgames = Integer.parseInt(auxcomas[4]);
                                break;
                            case "new":    
                                this.iLastAction = this.vGetNewActionStats();
                                this.endowmentusado = 0;
                                this.resulttot = 0;
                                break;
                            case "act":
                                this.endowmentusado  =  this.endowmentusado + this.iLastAction; 
                                msg.setOntology("HiAll");
                                msg.setContent("Action#"+this.iLastAction);
                                send(msg);                              
                                break;
                            case "res":
                                //System.out.println("Chegaron os resultados");
                                String aux2[] = mensaxe.getContent().split("#");
                                String resultados[] = aux2[1].split(",");
                                for(int i = 0; i< resultados.length;i++ ){
                                    this.resulttot = this.resulttot + Integer.parseInt(resultados[i]);
                                }
                                break;
                            case "gam":
                                this.add_action( (this.endowment+this.endowmentusado));
                                break;
                            default:
                                System.out.println("Esta chegando algo que non entendemos--->"+mensaxe.getContent());
                                break;
                        }
                    }else{
                        block();
                    }
                
               }catch(Exception e){
                   e.printStackTrace();
               }
            }
        }
            
        /**
         * Validates and extracts the parameters from the setup message
         *
         * @param msg ACLMessage to process
         * @return true on success, false on failure
         */
        private boolean validateSetupMessage(ACLMessage msg) throws NumberFormatException {
            int tN, tS, tR, tI, tP, tMyId;
            String msgContent = msg.getContent();

            String[] contentSplit = msgContent.split("#");
            if (contentSplit.length != 3) return false;
            if (!contentSplit[0].equals("Id")) return false;
            tMyId = Integer.parseInt(contentSplit[1]);

            String[] parametersSplit = contentSplit[2].split(",");
            if (parametersSplit.length != 5) return false;
            tN = Integer.parseInt(parametersSplit[0]);
            tS = Integer.parseInt(parametersSplit[1]);
            tR = Integer.parseInt(parametersSplit[2]);
            tI = Integer.parseInt(parametersSplit[3]);
            tP = Integer.parseInt(parametersSplit[4]);

            //At this point everything should be fine, updating class variables
            mainAgent = msg.getSender();
            N = tN;
            S = tS;
            R = tR;
            I = tI;
            P = tP;
            myId = tMyId;
            return true;
        }

        /**
         * Processes the contents of the New Game message
         * @param msgContent Content of the message
         * @return true if the message is valid
         */
        public boolean validateNewGame(String msgContent) {
            int msgId0, msgId1;
            String[] contentSplit = msgContent.split("#");
            if (contentSplit.length != 2) return false;
            if (!contentSplit[0].equals("NewGame")) return false;
            String[] idSplit = contentSplit[1].split(",");
            if (idSplit.length != 2) return false;
            msgId0 = Integer.parseInt(idSplit[0]);
            msgId1 = Integer.parseInt(idSplit[1]);
            if (myId == msgId0) {
                opponentId = msgId1;
                return true;
            } else if (myId == msgId1) {
                opponentId = msgId0;
                return true;
            }
            return false;
        }

        public void add_action(int puntos){
            int resultado =  (int)Math.round((Math.random()*100));
            if( (this.resulttot > (this.endowment+this.nplayers)/2) || (resultado>80) ){
                this.dtotalPayoffAction[iLastAction]  = this.dtotalPayoffAction[iLastAction] + puntos;
            }
        }

        public int vGetNewActionStats () {
            int maximo = 0;
            // Checking that I have played all actions before
            if(train != totaltrain){
                if (bAllActions != 4) {
                    bAllActions ++;
                    for (int i=0; i<iNumActions; i++){
                        
                        if (iNumTimesAction[i] == train) {
                            iNumTimesAction[i]++;
                            return possibleActions[i];
                        }
                    }

                }else{
                    bAllActions = 0;
                    train ++;
                    iNumTimesAction[0]++;
                    return possibleActions[0];
                }
            }else {
                // If all actions have been tested, the probabilities are adjusted
                maximo = 0;
                int indexmaximo = 0;
                for (int i=0; i<iNumActions; i++) {		
                    if(Math.round( dtotalPayoffAction[i]/iNumTimesAction[i]) > maximo   ){
                        maximo = Math.round( dtotalPayoffAction[i]/iNumTimesAction[i]);
                        indexmaximo = i;
                    }					                    
                }
                this.iLastAction = possibleActions[indexmaximo]; 
                return possibleActions[indexmaximo];
            }
            return 3;
        }

    }
}
