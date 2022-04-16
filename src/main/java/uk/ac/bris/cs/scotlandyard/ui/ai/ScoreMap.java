package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.*;

import java.util.ArrayList;
import java.util.List;

// A class to map the certain move with its assigned score
// The elements are marked as private, though they can be extracted with getters
public final class ScoreMap {

    List<Score> scoreMap = new ArrayList<>();
    final Board board;

    public ScoreMap(Board board) {
        this.board = board;
        this.scoreMap = getScoreMaps(board);
    }

    private final class Score {

        private Move move;
        private int score;

        private Score(final Move move, final int score) {
            this.move = move;
            this.score = score;
        }
    }

    // Getter to return detectives in the board
    private List<Piece> getDetectives(Board board) {
        List<Piece> detectives = board.getPlayers().asList();
        detectives.removeIf(p -> p.isMrX());
        return detectives;
    }

    // Getter to return the node value from a Move
    private Integer getDestination(Move move) {
        return move.accept(new Move.Visitor<>() {
            @Override
            public Integer visit(Move.SingleMove move) {
                return move.destination;
            }

            @Override
            public Integer visit(Move.DoubleMove move) {
                return move.destination2;
            }
        });
    }

    // Helper function that returns Enum Detective by its piece
    private Piece.Detective getDetectiveByPiece(Piece piece) {
        String colour = piece.webColour();
        Piece.Detective detective = switch (colour) {
            case "#f00" -> Piece.Detective.RED;
            case "#0f0" -> Piece.Detective.GREEN;
            case "#00f" -> Piece.Detective.BLUE;
            case "#fff" -> Piece.Detective.WHITE;
            case "#ff0" -> Piece.Detective.YELLOW;
            default -> throw new IllegalArgumentException("No detectives matched");
        };
        return detective;
    }


    // TODO When expanding, think of using these recursively (Predicting 'n' moves ahead)
    // TODO Didn't add whether detectives have required ticket to reach the destination
    // Helper function to return the nodes that detectives can potentially move when given the source
    private List<Integer> detectivePotential(Board board) {
        GameSetup setup = board.getSetup();
        List<Integer> potentialNode = new ArrayList<>();
        List<Piece> detectives = getDetectives(board);
        for(Piece p :detectives) {
            int source = board.getDetectiveLocation(getDetectiveByPiece(p))
                    .orElseThrow(NullPointerException :: new);
            for (int destination : setup.graph.adjacentNodes(source)) {
                potentialNode.add(destination);
            }
        }
        return potentialNode;
    }

    // Reference 'https://boardgamegeek.com/thread/102272/scotland-yard-basic-strategy' for basic strategy for mrX
    // Helper function to add score class to scoreMap

    private List<Score> getScoreMaps(Board board) {
        ImmutableSet<Move> mv = board.getAvailableMoves();
        for(Move m : mv) {
            scoreMap.add(score(m));
            System.out.println("By move m:" + m + "Score: " + score(m));
        }
        return scoreMap;
    }

    // TODO Basic points to be considered in the scoring function
    // ALL the information about the game should be accessed via 'board'
    // Score scales: -100 <= x <= 100 potentially ? (Can be mutated)
    // 1. Places which are near detectives get a low score
    // 1-1. If mrX's next move is the place where detectives can reach by one move, score -50 is allocated
    // 2. Place where has various choices of transportation gets high score
    // Main function to evaluate a move by assigning score
    private Score score(Move m) {
        int n = 0;
        List<Integer> potential = detectivePotential(board);
        for(int i :potential) {
            if(getDestination(m) == i) n -= 50;
        }
        return new Score(m, n);
    }


}
