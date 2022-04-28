import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.ui.ai.ScoreMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static uk.ac.bris.cs.scotlandyard.model.Piece.Detective.*;
import static uk.ac.bris.cs.scotlandyard.model.Piece.MrX.MRX;

public class TestScoreMap extends TestModel {

    TestScoreMap() throws IOException {
        testGetDetectives();
        testBestMove();
    }

    // Tests inner function getDetectives()
    public void testGetDetectives() throws IOException {
        var MrX = new Player(MRX, ScotlandYard.defaultMrXTickets(), 1);
        var Blue = new Player(BLUE, ScotlandYard.defaultDetectiveTickets(), 2);
        var Red = new Player(RED, ScotlandYard.defaultDetectiveTickets(), 3);
        var White = new Player(WHITE, ScotlandYard.defaultDetectiveTickets(), 4);

        Board board = createGameState().build(movesSetup(), MrX, Blue, Red, White);
        ScoreMap actual = new ScoreMap(board);
        List<Piece> actualDetective = actual.getDetectives();
        List<Piece> expectedAnswer = new ArrayList<>(Arrays.asList(BLUE, RED, WHITE));

        test(actualDetective.equals(expectedAnswer));
    }

    public void testBestMove() throws IOException {
        var MrX = new Player(MRX, makeTicketsManual(1,1,1,0,0), 120);
        var Blue = new Player(BLUE, ScotlandYard.defaultDetectiveTickets(), 121);
        Board board = createGameState().build(movesSetup(), MrX, Blue);
        ScoreMap actualSc = new ScoreMap(board);
        Move actual = actualSc.getBestMove();
        Move expected = new Move.SingleMove(MRX, MrX.location(), ScotlandYard.Ticket.TAXI, 144);
        test(actual.equals(expected));
    }
}