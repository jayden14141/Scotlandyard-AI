package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.*;

import java.util.ArrayList;
import java.util.List;

// A class to store Scores, given the board(the gameState)
public final class ScoreMap {

    private List<Score> scoreMap;
    private final Board board;

    public ScoreMap(final Board board) {
        this.board = board;
        this.scoreMap = createScoreMaps();
    }

    // A POJO to map the certain move with its assigned score
    // The elements are marked as private, though they can be extracted with getters
    public static final class Score {

        private final Move move;
        private int score;

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

        // Setter to update the score
        public void setScore(int value) {
            this.score = value;
        }

        @Override
        public String toString() {
            return "[Move: " + getMove() + ", score: " + getScore() + " ]";
        }
    }

    // Getter that returns ScoreMap
    public List<Score> getScoreMap() {
        return this.scoreMap;
    }

    // Helper function that returns detectives in the board
    private List<Piece> getDetectives() {
        List<Piece> detectives = new ArrayList<>(board.getPlayers());
        Piece mrX = null;
        for(Piece p: detectives) {
            if(p.webColour().equals("#000")) mrX = p;
        }
        detectives.remove(mrX);
        return detectives;
    }

    // Helper function that returns the node value from a Move
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

    // Helper function that returns an arrayList which contains
    // distance from the individual detectives to specific location
    private List<Integer> myDijkstra(int mrXPotential) {
        List<Piece> detectives = getDetectives();
        List<Integer> detectivesDistance = new ArrayList<>();
        for (Piece p :detectives) {
            int source = board.getDetectiveLocation(getDetectiveByPiece(p))
                    .orElseThrow(NullPointerException :: new);
            Dijkstra my = new Dijkstra(board, source);
            detectivesDistance.add(my.printWeight(mrXPotential));
        }
        return detectivesDistance;
    }

    // TODO Didn't add whether detectives have required ticket to reach the destination
    private void evaluateByDistance(Move m, Score s) {
        List <Integer> distance = myDijkstra(getDestination(m));
        int n = getDetectives().size();
        int meanDistance = 1;

        // Assigns basic score by mean distance of detectives
        for (int j : distance) {
            meanDistance += j / n;
        }
        s.setScore(s.getScore() - (100 / meanDistance));

        // Updates score if detectives are nearby the location where mrX is trying to go
        for(int i : distance) {
            if(i == 1) s.setScore(s.getScore() - 40);
            else if (i == 2) s.setScore(s.getScore() - 10);
        }
    }

    // Helper function to find out whether the move is single move or not
    private boolean isSingleMove(Move m) {
        return m.accept(new Move.Visitor<Boolean>() {
            @Override
            public Boolean visit(Move.SingleMove move) {
                return true;
            }

            @Override
            public Boolean visit(Move.DoubleMove move) {
                return false;
            }
        });
    }

    private boolean RevealedLocation() {
        int previousRound = board.getMrXTravelLog().size();
        for (int i : ScotlandYard.REVEAL_MOVES) {
            if (i == previousRound) return true;
        }
        return false;
    }

    // Helper function to find out if mrX is on the cusp (Just revealed his move / Detectives are so close)
    // Utilises it to make another action for mrX
    private boolean isEmergency() {
        return RevealedLocation();
    }

    // Helper function that uses functional visitor to handle with single move and double move
    private int handleMoves(Move m) {
        return m.accept(new Move.FunctionalVisitor<>(
                singleMove -> returnScoreSm(singleMove), doubleMove -> returnScoreDm(doubleMove)));
    }

    // Helper function that handles single move
    // Makes two different action whether mrX is on the cusp or not
    private int returnScoreSm(Move.SingleMove m) {
        ScotlandYard.Ticket t = m.ticket;
        ScotlandYard.Ticket[] preferred;
        int rank = 0;
        int score = 0;
        if(!isEmergency()) {
            score += 10;
            preferred = new ScotlandYard.Ticket[]{ScotlandYard.Ticket.TAXI, ScotlandYard.Ticket.BUS,
                    ScotlandYard.Ticket.UNDERGROUND, ScotlandYard.Ticket.SECRET};
        } else {
            score -= 10;
            preferred = new ScotlandYard.Ticket[]{ScotlandYard.Ticket.SECRET, ScotlandYard.Ticket.UNDERGROUND,
                    ScotlandYard.Ticket.BUS, ScotlandYard.Ticket.TAXI};
        }
        for(int i = 0; i < 4; i++) {
            if (preferred[i].equals(t)) rank = i + 1;
        }

        score = switch (rank) {
            case 1 -> 10;
            case 2 -> 5;
            case 3 -> 3;
            case 4 -> -1;
            default -> throw new IllegalArgumentException();
        };

        return score;
    }

    // Helper function that handles single move
    // Makes two different action whether mrX is on the cusp or not
    // TODO Not done
    private int returnScoreDm(Move.DoubleMove m) {
        int dif = 0;
        if (!isEmergency()) dif -= 10;
        else dif += 10;
        return dif;
    }

    private void evaluateByTransport(Move m, Score s) {
        int difference = handleMoves(m);
        s.setScore(s.getScore() + difference);
    }

    // TODO Basic points to be considered in the scoring function
    // ALL the information about the game should be accessed via 'board'
    // Score scales: -100 <= x <= 100 potentially ? (Can be mutated)
    // 1. Places which are near detectives get a low score (O)
    // 1-1. If mrX's next move is the place where detectives can reach by one move, score -40 is allocated (O)
    // 2. Place where has various choices of transportation gets high score
    // 3. Try varying scores on types of transportation
    // TODO Evaluate current situation to act differently
    // Main function to evaluate a move by assigning score
    private Score score(Move m) {
        Score s = new Score(m, 0);
        evaluateByDistance(m, s);
        evaluateByTransport(m, s);
        return s;
    }

    // Reference 'https://boardgamegeek.com/thread/102272/scotland-yard-basic-strategy' for basic strategy for mrX
    // Helper function to add score class to scoreMap
    private List<Score> createScoreMaps() {
        ImmutableSet<Move> mv = board.getAvailableMoves();
        List<Score> scoreList = new ArrayList<>();
        for(Move m : mv) {
            scoreList.add(score(m));
        }
        return scoreList;
    }

}
