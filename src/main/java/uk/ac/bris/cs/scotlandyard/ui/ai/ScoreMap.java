package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// A class to store Scores, given the board(the gameState)
public final class ScoreMap {

    private final List<Score> scoreMap;
    private final Board board;
    private final Move bestMove;

    public ScoreMap(final Board board) {
        this.board = board;
        this.scoreMap = createScoreMaps();
        this.bestMove = bestMove();
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


        @Override
        public String toString() {
            return "[Move: " + this.move + ", score: " + this.score + " ]";
        }
    }

    // Getter that returns the best move
    public Move getBestMove() {
        return bestMove;
    }

    // Helper function that returns detectives in the board
    public List<Piece> getDetectives() {
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
        return switch (colour) {
            case "#f00" -> Piece.Detective.RED;
            case "#0f0" -> Piece.Detective.GREEN;
            case "#00f" -> Piece.Detective.BLUE;
            case "#fff" -> Piece.Detective.WHITE;
            case "#ff0" -> Piece.Detective.YELLOW;
            default -> throw new IllegalArgumentException("No detectives matched");
        };
    }

    // Helper function that returns an arrayList which contains
    // distance from the individual detectives to specific location
    private List<Integer> myDijkstra(int mrXPotential) {
        List<Piece> detectives = getDetectives();
        List<Integer> detectivesDistance = new ArrayList<>();
        for (Piece p :detectives) {
            int source = board.getDetectiveLocation(getDetectiveByPiece(p))
                    .orElseThrow(NullPointerException :: new);
            detectivesDistance.add(new Dijkstra(board, source, p).printWeight(mrXPotential));
        }
        return detectivesDistance;
    }

    //Calculates how dangerous the location will be to mrX depending on an adjusted mean distance from all detectives
    private List<Float> calculateDanger(List<Integer> distance){
        List<Float> dangerList = new ArrayList<>();
        int roundRemaining = board.getMrXTravelLog().size();
        for (int intDistance : distance){
            if (!(intDistance > roundRemaining)) {
                dangerList.add((float) (1 - (intDistance / roundRemaining)));
            }
        }
        return dangerList;
    }

    // Evaluates score by distance to/from detectives
    private void evaluateByDistance(Move m, Score s) {
        List <Integer> distance = myDijkstra(getDestination(m));
        List <Float> dangerList = calculateDanger(distance);
        float meanDanger = 0;
        for (float danger : dangerList){
            meanDanger += danger;
        }
        meanDanger = meanDanger/4;
        s.score -= Math.round(50*meanDanger);
        // Updates score if detectives are nearby the location where mrX is trying to go
        for(int i : distance) {
            if(i == 1) s.score -= 60;
        }
    }

    // Helper function that returns boolean whether mrX is about to reveal his location
    private boolean aboutToRevealLocation() {
        int nextRound = board.getMrXTravelLog().size() + 1;
        for (int i : ScotlandYard.REVEAL_MOVES) {
            if (i == nextRound) return true;
        }
        return false;
    }

    // Helper function that returns boolean whether mrX has revealed his location in his last turn
    private boolean revealedLocation() {
        int previousRound = board.getMrXTravelLog().size();
        for (int i : ScotlandYard.REVEAL_MOVES) {
            if (i == previousRound) return true;
        }
        return false;
    }

    // Helper function returning if detective can catch mrX by one move
    public boolean detectiveIsClose() {
        var moves = board.getAvailableMoves().asList();
        List <Integer> distance = myDijkstra(moves.get(0).source());
        for(int i: distance) {
            if(i == 1) return true;
        }
        return false;
    }




    // Helper function to map the evaluated score with tickets used for the move
    private int evaluateTicket(ScotlandYard.Ticket[] prefer, ScotlandYard.Ticket t) {
        int score;
        int rank = 0;
        for(int i = 0; i < 4; i++) {
            if (prefer[i].equals(t)) rank = i + 1;
        }
        if (rank == 1) score = 10;
        else if (rank == 2) score = 5;
        else if (rank == 3) score = -5;
        else if (rank == 4) score = -10;
        else throw new IllegalArgumentException();
        return score;
    }

    //helper function to check if node has only 1 transport, prevents use of secret move when redundant
    private boolean singleTransportNode(int source){
        int[] typesOfTransport = howManyTransport();
        return typesOfTransport[source] == 1;
    }

    // Helper function that handles single move
    // Makes two different action whether mrX is on the cusp or not
    private int returnScoreSm(Move.SingleMove m) {
        ScotlandYard.Ticket t = m.ticket;
        ScotlandYard.Ticket[] preferred;
        int score = 0;
        if(revealedLocation() && !singleTransportNode(m.source())) {
            score -= 10;
            preferred = new ScotlandYard.Ticket[]{ScotlandYard.Ticket.SECRET, ScotlandYard.Ticket.UNDERGROUND,
                    ScotlandYard.Ticket.BUS, ScotlandYard.Ticket.TAXI};
        } else {
            preferred = new ScotlandYard.Ticket[]{ScotlandYard.Ticket.TAXI, ScotlandYard.Ticket.BUS,
                    ScotlandYard.Ticket.UNDERGROUND, ScotlandYard.Ticket.SECRET};
        }
        score += evaluateTicket(preferred, t);

        return score;
    }

    // Helper function that handles single move
    // Makes two different actions whether mrX is on the cusp or not
    private int returnScoreDm(Move.DoubleMove m) {
        int score = 0;
        ScotlandYard.Ticket[] preferred;
        ScotlandYard.Ticket t1 = m.ticket1;
        ScotlandYard.Ticket t2 = m.ticket2;
        // Prevents using secret ticket twice in one move
        if ((m.ticket1 == ScotlandYard.Ticket.SECRET) && (m.ticket2 == ScotlandYard.Ticket.SECRET)) score -= 20;
        if (revealedLocation()) {
            score += 30;
            preferred = new ScotlandYard.Ticket[]{ScotlandYard.Ticket.SECRET, ScotlandYard.Ticket.UNDERGROUND,
                    ScotlandYard.Ticket.BUS, ScotlandYard.Ticket.TAXI};
        }
        // Prevents overusing double tickets
        else {
            score -= 50;
            preferred = new ScotlandYard.Ticket[]{ScotlandYard.Ticket.TAXI, ScotlandYard.Ticket.BUS,
                    ScotlandYard.Ticket.UNDERGROUND, ScotlandYard.Ticket.SECRET};
        }
        score += evaluateTicket(preferred, t1);
        score += evaluateTicket(preferred, t2);

        return score;
    }

    // Helper function that uses functional visitor to handle with single move and double move
    private void evaluateByTransport(Move m, Score s) {
        s.score += m.accept(new Move.FunctionalVisitor<>(
                this::returnScoreSm, this::returnScoreDm));
    }

    // Helper function that returns how many types of transportation is run in the specific node
    private int[] howManyTransport() {
        var graph = board.getSetup().graph;
        int n = graph.nodes().size();
        int[] transport = new int[n + 1];
        for (int i = 1; i < n + 1; i++) {
            transport[i] = 0;
            boolean taxi = false;
            boolean bus = false;
            boolean underground = false;
            boolean ferry = false;

            for (int j : graph.adjacentNodes(i)) {
                if (graph.edgeValue(i,j).equals(Optional.of(ImmutableSet.of(
                        ScotlandYard.Transport.TAXI)))) taxi = true;
                else if (graph.edgeValue(i,j).equals(Optional.of(ImmutableSet.of(
                        ScotlandYard.Transport.BUS)))) bus = true;
                else if (graph.edgeValue(i,j).equals(Optional.of(ImmutableSet.of(
                        ScotlandYard.Transport.UNDERGROUND)))) underground = true;
                else if (graph.edgeValue(i,j).equals(Optional.of(ImmutableSet.of(
                        ScotlandYard.Transport.FERRY)))) ferry = true;
            }

            if (taxi) transport[i]++;
            if (bus) transport[i]++;
            if (underground) transport[i]++;
            if (ferry) transport[i]++;
        }
        return transport;
    }


    // Helper function to evaluate the node on the graph
    // Node which has more variation of transport gets a high score
    // If mrX is about to reveal his location, more score is assigned if the node is transferred
    private void evaluateByNode(Move m, Score s) {
        int[] transport = howManyTransport();
        int destination = getDestination(m);
        int quantity = transport[destination];
        if (aboutToRevealLocation() || detectiveIsClose()) {
            switch (quantity) {
                case 1 -> s.score -= 2;
                case 2 -> s.score += 20;
                case 3 -> s.score += 30;
                case 4 -> s.score += 50;
                default -> throw new IllegalArgumentException("In" + m + quantity);
            }
        } else {
            switch (quantity) {
                case 1 -> s.score -= 3;
                case 2 -> s.score += 5;
                case 3 -> s.score += 10;
                case 4 -> s.score += 25;
                default -> throw new IllegalArgumentException("In" + m + quantity);
            }
        }
    }


    // Main function to evaluate a move by assigning score
    // 1. Places which are near detectives get a low score (evaluateByDistance)
    // 2. Varying scores on types of transportation (evaluateByTransport)
    // 3. Place where has various choices of transportation gets high score (evaluateByNode)
    private Score score(Move m) {
        Score s = new Score(m, 0);
        evaluateByDistance(m, s);
        evaluateByTransport(m, s);
        evaluateByNode(m, s);
        return s;
    }

    // Helper function to add score class to scoreMap
    private List<Score> createScoreMaps() {
        ImmutableSet<Move> mv = board.getAvailableMoves();
        List<Score> scoreList = new ArrayList<>();
        for(Move m : mv) {
            scoreList.add(score(m));
        }
        return scoreList;
    }

    // Helper function to return the best move among the moves derived from the score
    // Compares score value from a map and returns the corresponding key(Move)
    private Move bestMove() {
        ScoreMap.Score best = scoreMap.get(0) ;
        for(ScoreMap.Score s :scoreMap) {
            if (s.getScore() >= best.getScore()) {
                best = s;
            }
        }
        return best.getMove();
    }
}
