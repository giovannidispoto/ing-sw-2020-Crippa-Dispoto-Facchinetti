package it.polimi.ingsw.client.network.commands.lobbyPhase;

import it.polimi.ingsw.client.controller.ClientController;
import it.polimi.ingsw.client.controller.WaitManager;
import it.polimi.ingsw.client.network.commands.Command;
import it.polimi.ingsw.client.network.messagesInterfaces.dataInterfaces.lobbyPhase.PlayerInterface;

import java.util.List;

/**
 * Class that manages the command: GetPlayers
 */
public class GetPlayersCommand implements Command {
    private final List<PlayerInterface> players;

    /**
     * Create command
     * @param players players in game
     */
    public GetPlayersCommand(List<PlayerInterface> players) {
        this.players = players;
    }

    @Override
    public void execute(ClientController clientController) {
        clientController.setPlayers(this.players);

        for(PlayerInterface player : this.players){
            //Find player nickname same as that of this client
            if(player.getPlayerNickname().equals(clientController.getPlayerNickname())){
                //set the color assigned by the server
                clientController.setPlayerColor(player.getColor());
            }
        }
        //Awakens who was waiting Server Response
        synchronized (WaitManager.waitGetPlayers){
            WaitManager.waitGetPlayers.setUsed();
            WaitManager.waitGetPlayers.notify();
        }
    }
}
