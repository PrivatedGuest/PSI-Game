1)Lionel Salgado Rigueira

2) 

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
    5.2)I got a "configuration.txt" just to have persistence in the opcions taken
    5.3)There is a real time ranking in the UI.