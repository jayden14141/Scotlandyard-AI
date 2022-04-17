package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.*;

import java.util.ArrayList;
import java.util.List;

// A class to store Scores, given the board(the gameState)
public final class ScoreMapMrX {

    List<Score> scoreMap;
    Board board;

    public ScoreMapMrX(final Board board) {
        this.board = board;
        this.scoreMap = getScoreMaps();
    }

    // A POJO to map the certain move with its assigned score
    // The elements are marked as private, though they can be extracted with getters
    public final class Score {

        private final Move move;
        private final int score;

        private Score(Move move, int score) {
            this.move = move;
            this.score = score;
        }

        // Getter to return the score
        public int getScore() {
            return score;
        }

        // Getter to return the move
        public Move getMove() {
            return move;
        }

        @Override
        public String toString() {
            return "[Move: " + getMove() + ", score: " + getScore() + " ]";
        }
    }

    // Getter to return detectives in the board
    private List<Piece> getDetectives() {
        List<Piece> detectives = new ArrayList<>(board.getPlayers());
        Piece mrX = null;
        for(Piece p: detectives) {
            if(p.webColour().equals("#000")) mrX = p;
        }
        detectives.remove(mrX);
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
    private List<Integer> detectivePotential() {
        GameSetup setup = board.getSetup();
        List<Integer> potentialNode = new ArrayList<>();
        List<Piece> detectives = getDetectives();
        for(Piece p :detectives) {
            int source = board.getDetectiveLocation(getDetectiveByPiece(p))
                    .orElseThrow(NullPointerException :: new);
            potentialNode.addAll(setup.graph.adjacentNodes(source));
        }
        return potentialNode;
    }
    // TODO Merge this function with detectivePotential() above
    public void handleDijkstra(int destination) {
        List<Piece> detectives = getDetectives();
        for (Piece p : detectives) {
            int source = board.getDetectiveLocation(getDetectiveByPiece(p))
                    .orElseThrow(NullPointerException :: new);
            Dijkstra dj = new Dijkstra(board, source, destination);
            System.out.println("@"+ p +": Distance from (" + source + ")to (" + destination +") :" + dj.getDistance(destination));
        }
    }

    // Reference 'https://boardgamegeek.com/thread/102272/scotland-yard-basic-strategy' for basic strategy for mrX
    // Helper function to add score class to scoreMap
    private List<Score> getScoreMaps() {
        ImmutableSet<Move> mv = board.getAvailableMoves();
        List<Score> scoreList = new ArrayList<>();
        for(Move m : mv) {
            handleDijkstra(m.source());
            scoreList.add(score(m));
//            System.out.println(score(m));
        }
        return scoreList;
    }


    // TODO Basic points to be considered in the scoring function
    // ALL the information about the game should be accessed via 'board'
    // Score scales: -100 <= x <= 100 potentially ? (Can be mutated)
    // 1. Places which are near detectives get a low score
    // 1-1. If mrX's next move is the place where detectives can reach by one move, score -20 is allocated
    // 2. Place where has various choices of transportation gets high score
    // 3.Try varying scores on types of transportation
    // TODO Evaluate current situation to act differently
    // Main function to evaluate a move by assigning score
    private Score score(Move m) {
        int n = 0;
        List<Integer> potential = detectivePotential();
        //TODO We can implement dijkstra algorithm (Outer class) to calculate how far detectives are
        for(int i :potential) {
            if(getDestination(m) == i) n -= 20;
        }
        return new Score(m, n);
    }


}
