package it.polimi.ingsw.client.network.commands.finishPhase;

import it.polimi.ingsw.client.controller.ClientController;
import it.polimi.ingsw.client.controller.ExceptionMessages;
import it.polimi.ingsw.client.controller.GameState;
import it.polimi.ingsw.client.network.commands.Command;

/**
 * Class that manages the command: Win
 */
public class WinCommand implements Command {
    @Override
    public void execute(ClientController clientController) {
        clientController.setGameExceptionMessage(ExceptionMessages.winMessage, GameState.FINISH, true);
    }
}
