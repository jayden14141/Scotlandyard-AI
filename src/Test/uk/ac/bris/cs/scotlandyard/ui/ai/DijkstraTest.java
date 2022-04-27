package uk.ac.bris.cs.scotlandyard.ui.ai;

import org.junit.Test;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Player;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;

import static uk.ac.bris.cs.scotlandyard.model.Piece.Detective.BLUE;
import static uk.ac.bris.cs.scotlandyard.model.Piece.Detective.GREEN;
import static uk.ac.bris.cs.scotlandyard.model.Piece.MrX.MRX;

import static org.junit.Assert.*;

public class DijkstraTest extends ModelBase {

    @Test public void howItWorks() {
        // Change the location, and feel free to use it
        // many functions can be accessed via variable state

        var MrX = new Player(MRX, ScotlandYard.defaultMrXTickets(), 1);
        var Blue = new Player(BLUE, ScotlandYard.defaultMrXTickets(), 2);
        Board.GameState state = getState().build(getSetup(), MrX, Blue);

    }

    @Test public void testInitialisation() {
//        gameStateFactory.build();
    }
}