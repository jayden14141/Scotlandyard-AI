
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Player;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;
import uk.ac.bris.cs.scotlandyard.ui.ai.Dijkstra;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static uk.ac.bris.cs.scotlandyard.model.Piece.Detective.BLUE;
import static uk.ac.bris.cs.scotlandyard.model.Piece.Detective.RED;
import static uk.ac.bris.cs.scotlandyard.model.Piece.MrX.MRX;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.TAXI;

public class TestDijkstra extends TestModel {

    TestDijkstra() throws IOException {
        testDijkstraReadsGraphCorrectly();
        testDestinationToSourcePrintsZero();
        testDijkstraForManyPlayers();
        testAfterAdvance();
        testNoTicketDoesNotCalculateMove();
        testWithGetAvailableMoves();
    }


    // Tests whether Dijkstra class reads graph correctly, excluding ferry tickets
    public void testDijkstraReadsGraphCorrectly() throws IOException {
        var MrX = new Player(MRX, ScotlandYard.defaultMrXTickets(), 78);
        var Blue = new Player(BLUE, ScotlandYard.defaultDetectiveTickets(), 194);
        Board board = createGameState().build(movesSetup(), MrX, Blue);
        Dijkstra actual = new Dijkstra(board, Blue.location(), BLUE);
        int[][] actualEdges = actual.getEdges();
        ArrayList<Integer> actualNode = new ArrayList<>();
        for (int i = 1; i <= 199; i++) {
            if (actualEdges[194][i] == 1) actualNode.add(i);
        }
        ArrayList<Integer> expectedNode = new ArrayList<>(Arrays.asList(192,193,195));
        test(expectedNode, actualNode);
    }

    // Tests whether it prints the distance as zero if source == destination
    public void testDestinationToSourcePrintsZero() throws IOException {
        var MrX = new Player(MRX, ScotlandYard.defaultMrXTickets(), 46);
        var Blue = new Player(BLUE, ScotlandYard.defaultDetectiveTickets(), 50);
        Board board = createGameState().build(movesSetup(), MrX, Blue);
        Dijkstra actual = new Dijkstra(board, Blue.location(), BLUE);
        int actualDistance = actual.printWeight(Blue.location());
        int expectedDistance = 0;
        test(actualDistance == expectedDistance);
    }

    public void testDijkstraForManyPlayers() throws IOException {
        var MrX = new Player(MRX, ScotlandYard.defaultMrXTickets(), 67);
        var Blue = new Player(BLUE, ScotlandYard.defaultDetectiveTickets(), 26);
        var Red = new Player(RED, ScotlandYard.defaultDetectiveTickets(), 111);

        Board board = createGameState().build(movesSetup(), MrX, Blue, Red);
        Dijkstra actual1 = new Dijkstra(board, Blue.location(), BLUE);
        Dijkstra actual2 = new Dijkstra(board, Red.location(), RED);
        int actualDistance1 = actual1.printWeight(MrX.location());
        // 26 -> 39 -> 51 -> 67
        int expectedDistance1 = 3;
        test(actualDistance1 == expectedDistance1);
        int actualDistance2 = actual2.printWeight(MrX.location());
        // 111 -> 67
        int expectedDistance2 = 1;
        test(actualDistance2 == expectedDistance2);
    }

    public void testAfterAdvance() throws IOException {
        var MrX = new Player(MRX, ScotlandYard.defaultMrXTickets(), 67);
        var Red = new Player(RED, ScotlandYard.defaultDetectiveTickets(), 111);
        Board board1 = createGameState().build(movesSetup(), MrX, Red);
        Dijkstra actual1 = new Dijkstra(board1, Red.location(), RED);
        int actualDistance1 = actual1.printWeight(67);
        int expectedDistance1 = 1;
        test(actualDistance1 == expectedDistance1);
        Board board2 = createGameState().build(movesSetup(), MrX, Red).advance(
                new Move.SingleMove(MRX, MrX.location(), TAXI, 68));
        Dijkstra actual2 = new Dijkstra(board1, Red.location(), RED);
        int actualDistance2 = actual2.printWeight(68);
        int expectedDistance2 = 2;
        test(actualDistance2 == expectedDistance2);
    }

    public void testWithGetAvailableMoves() throws IOException {
        // Made tickets manual just to avoid too many available moves
        var MrX = new Player(MRX, makeTicketsManual(1,1,1,0,0), 120);
        var Blue = new Player(BLUE, ScotlandYard.defaultDetectiveTickets(), 145);
        Board board = createGameState().build(movesSetup(), MrX, Blue);
        Dijkstra actual = new Dijkstra(board, Blue.location(), BLUE);
        int expectedDistance = 1;
        // In any ways, the detective requires a single move to reach mrX's potential moves' destination
        for (Move m :board.getAvailableMoves()) {
            test(actual.printWeight(m.accept(new Move.Visitor<>() {
                @Override
                public Integer visit(Move.SingleMove move) {
                    return move.destination;
                }

                @Override
                public Integer visit(Move.DoubleMove move) {
                    return move.destination2;
                }
            })) == expectedDistance);
        }
    }

    // Tests that the algorithm doesn't calculate the distance
    // if the detective have not enough tickets to reach the destination
    public void testNoTicketDoesNotCalculateMove() throws IOException {
        var MrX = new Player(MRX, ScotlandYard.defaultMrXTickets(), 46);
        var Blue = new Player(BLUE, makeTicketsManual(0,0,0,0,0), 50);
        Board board = createGameState().build(movesSetup(), MrX, Blue);
        Dijkstra actual = new Dijkstra(board, Blue.location(), BLUE);
        int actualDistance = actual.printWeight(45);
        // Default destination 1000
        int expectedDistance = 1000;
        test(actualDistance == expectedDistance);
    }
}

