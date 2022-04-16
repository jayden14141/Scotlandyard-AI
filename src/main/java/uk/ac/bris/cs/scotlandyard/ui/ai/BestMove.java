package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.*;

import java.util.*;

public final class BestMove {

    Map<Move, Integer> scoreMap;
    Move bestMove;

    public BestMove(Board board) {
        scoreMap = getScoreMap(board, board.getAvailableMoves());
        bestMove = bestMove(scoreMap);
    }

    // Getter to return the best Move
    public Move getBestMove() {
        return this.bestMove;
    }

    // Getter to return detectives in the board
    public List<Piece> getDetectives(Board board) {
        List<Piece> detectives = board.getPlayers().asList();
        detectives.removeIf(p -> p.isMrX());
        return detectives;
    }

    // Getter to return the node value from a Move
    public Integer getDestination(Move move) {
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

    // Helper function to return the best move among the moves derived from the score
    // Compares score value from a map and returns the corresponding key(Move)
    private Move bestMove(Map<Move,Integer> scoreMap) {
        return Collections.max(scoreMap.entrySet(), Map.Entry.comparingByValue())
                .getKey();
    }

    // Helper function that returns Enum Detective by its piece
    private Piece.Detective getDetectiveByPiece(Piece piece) {
        String colour = piece.webColour();
        Piece.Detective detective;
        switch(colour) {
            case "#f00" : detective = Piece.Detective.RED; break;
            case "#0f0" : detective = Piece.Detective.GREEN; break;
            case "#00f" : detective = Piece.Detective.BLUE; break;
            case "#fff" : detective = Piece.Detective.WHITE; break;
            case "#ff0" : detective = Piece.Detective.YELLOW; break;
            default : throw new IllegalArgumentException("No detectives matched");
        }
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
    // Helper function to map scores with moves
    // Used Map<Move, Int> as a return value; Indicating Move(Move) and score(Int) respectively
    private Map<Move, Integer> getScoreMap(Board board, ImmutableSet<Move> mv) {
        Map<Move, Integer> scoreMap = new HashMap<>();
        for(Move m : mv) {
            scoreMap.put(m, score(board, m));
            System.out.println("By move m:" + m + "Score: " + score(board,m));
        }
        return scoreMap;
    }

    // TODO Basic points to be considered in the scoring function
    // ALL the information about the game should be accessed via 'board'
    // Score scales: -100 <= x <= 100 potentially ? (Can be mutated)
    // 1. Places which are near detectives get a low score
    // 1-1. If mrX's next move is the place where detectives can reach by one move, score -50 is allocated
    // 2. Place where has various choices of transportation gets high score
    private Integer score(Board board, Move m) {
        int n = 0;
        List<Integer> potential = detectivePotential(board);
        for(int i :potential) {
            if(getDestination(m) == i) return -50;
        }
        return n;
    }
}
