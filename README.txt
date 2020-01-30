1)Lionel Salgado Rigueira

2) COMPILAR-->javac -cp .\jade\lib\jade.jar *.java agents/*.java
	nota:Cambiar o classpath do jade
   EXECUTAR-->java -cp ".\jade\lib\jade.jar;.;/agents" jade.Boot -nomtp -agents "Principal:MainAgent;Random:agents.RandomPlayer;Statistical:agents.MyStatisticalPlayer;Friendly:agents.tftfriendlyPlayer;TFTRemodel:agents.tftremodelPlayer;Fixed:agents.FixedPlayer;Random2:agents.RandomPlayer;Random3:agents.RandomPlayer"


3)  Note: We dont really know when a dissaster appear, so we will use 0.8 in our 
          agents and and it is suppost to be in accord in late game.    
    3.1)Random player plays random value each round
    3.2)Fixed  player choose and option when he got her ID and repeat it
    3.3)MyStatisticalPlayer:The algorith is trained  X times play   ing all values it can play, then he will play the value he got the
                            best results with. IF he keep plaaying the same and the average change, the IA will changue the value too.
    3.4)tftremodelPlayer: I tried to create one algorith with same idea as tit for tat because it was really good at axelrod´s tournament.
                1)Start the game giving what everyone is suppose to give to avoid the dissaster
                2)In the second round, the agent play what the other agents did in the first round(rounded)
    3.5)tftfriendlyPlayer: We also start the game as tftremodel.If the sum of all values is above the threshold/rounds,we contribute 1 point less
                            Would we reach the treshold in this round doing "all in"?  we contribute with 1 point more : we play what we are suppose to



4)MyStatisticalPlayer

5) 
    5.1)Agents are concurrent each other,so its possible to see some warning (even controlated exceptions) during the execution in the CLI(not UI).
    5.2)I got a "configuration.txt" just to have persistence in the options taken
    5.3)There is a real time ranking in the UI.

    CONCURRENCY--> The main send messages to all the agents(playing) and they play concurrently but they have one exception using the send() method from the jade library

		java.util.ConcurrentModificationException
        		at java.util.ArrayList$Itr.checkForComodification(ArrayList.java:909)
        		at java.util.ArrayList$Itr.next(ArrayList.java:859)
        		at jade.util.leap.ArrayList$1.next(ArrayList.java:162)
        		at jade.core.AgentContainerImpl.handleSend(AgentContainerImpl.java:740)
        		at jade.core.Agent.send(Agent.java:1928)
        		at agents.tftfriendlyPlayer$Play.action(tftfriendlyPlayer.java:104) /*THIS IS THE SEND() INSTRUCTION*/
        		at jade.core.behaviours.Behaviour.actionWrapper(Behaviour.java:344)
        		at jade.core.Agent$ActiveLifeCycle.execute(Agent.java:1585)
        		at jade.core.Agent.run(Agent.java:1524)
        		at java.lang.Thread.run(Thread.java:748)

	Conclusion-> The agents are concurrent and the program should be faster, but all that exceptions make it slower and hard to debug. In this situacions concurrency is not
worth, even if it works well.



