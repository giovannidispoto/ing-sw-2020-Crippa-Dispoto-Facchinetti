package it.polimi.ingsw.model.parser;

import it.polimi.ingsw.model.cards.Deck;
import it.polimi.ingsw.model.cards.Type;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

class DeckReaderTest {
    String effectApollo ="Your Move: Your Worker may move into an opponent Worker’s space by forcing their Worker to the space yours just vacated";

    @Test
    void readerTest() throws IOException {

        DeckReader deckReader = new DeckReader();
        //load from *.json
        Deck deck = deckReader.loadDeck(new FileReader("src/Divinities.json"));

        assertNotNull(deck.getDivinityCard("Apollo"));
        assertSame(deck.getDivinityCard("Apollo").getCardType(), Type.MOVEMENT);
        assertSame(deck.getDivinityCard("Apollo").getNumberOfPlayersAllowed(), 3);
        assertEquals(deck.getDivinityCard("Apollo").getCardEffect(), effectApollo);
        assertNotNull(deck.getDivinityCard("Demeter"));
        assertSame(deck.getDivinityCard("Demeter").getCardType(), Type.BUILD);
        assertSame(deck.getDivinityCard("Demeter").getNumberOfPlayersAllowed(), 3);
        assertNotNull(deck.getDivinityCard("Chronus"));
        assertSame(deck.getDivinityCard("Chronus").getCardType(), Type.WIN);
        assertSame(deck.getDivinityCard("Chronus").getNumberOfPlayersAllowed(), 2);
    }
}