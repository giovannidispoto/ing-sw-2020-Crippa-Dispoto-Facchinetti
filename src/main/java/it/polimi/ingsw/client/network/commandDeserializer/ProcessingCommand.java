package it.polimi.ingsw.client.network.commandDeserializer;

import com.google.gson.JsonElement;
import it.polimi.ingsw.client.network.commands.Command;

/**
 *  Interface required to deserialize through hashmap
 */
public interface ProcessingCommand {
    Command command(JsonElement jsonElement);
}
