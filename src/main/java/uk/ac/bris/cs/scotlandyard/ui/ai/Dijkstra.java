package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.GameSetup;
import uk.ac.bris.cs.scotlandyard.model.Piece;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;

import java.util.*;

public class Dijkstra {

    private final Board board;
    // Maximum nodes
    private final int MAX;
    private final int source;
    private int[][] edges;
    private boolean[] visited;
    private Node [] shortestP;
    private PriorityQueue<Node> pq;
    private Piece p;
    private Map<ScotlandYard.Ticket, Integer> ticketMap;

    // Helper inner class to store information about the node and the distance from the source
    public static class Node implements Comparable<Node> {
        private final int node;
        private int distance;

        public Node(int node, int distance) {
            this.node = node;
            this.distance = distance;
        }


        // Mandatory to use these in priority queue
        @Override
        public int compareTo(Node o) {
            return Integer.compare(this.distance, o.distance);
        }

        @Override
        public String toString() {
            return "To" + node + ": DISTANCE" + distance;
        }
    }

    public Dijkstra(Board board, int source, Piece p) {

        this.board = board;
        this.MAX = board.getSetup().graph.nodes().size();
        this.source = source;
        this.p = p;
        this.ticketMap = new HashMap<>();
        initTickets();
        initialise();
        dijkstra();
    }

    public int[][] getEdges() {
        return edges;
    }

    private void initTickets() {
        Board.TicketBoard ticketBoard = board.getPlayerTickets(p).orElseThrow();
        Map<ScotlandYard.Ticket, Integer> newTickets = new HashMap<>();
        newTickets.put(ScotlandYard.Ticket.TAXI, ticketBoard.getCount(ScotlandYard.Ticket.TAXI));
        newTickets.put(ScotlandYard.Ticket.BUS, ticketBoard.getCount(ScotlandYard.Ticket.BUS));
        newTickets.put(ScotlandYard.Ticket.UNDERGROUND, ticketBoard.getCount(ScotlandYard.Ticket.UNDERGROUND));
        this.ticketMap = newTickets;
    }

    // Initialises the basic dijkstra class
    private void initialise() {
        edges = new int[MAX+1][MAX+1];
        shortestP = new Node[MAX+1];
        visited = new boolean[MAX+1];
        pq = new PriorityQueue<>();

        for(int i = 1; i < MAX+1; i++) {
            for(int j = 1; j < MAX+1; j++) {
                if(i == j) {
                    edges[i][j] = 0;
                    edges[j][i] = 0;
                }
                else {
                    edges[i][j] = -1;
                    edges[j][i] = -1;
                }
            }
            shortestP[i] = new Node(i, 1000);
            visited[i] = false;
        }
        shortestP[source].distance = 0;
        makeEdges(board);

    }

    // Reforms data from the graph
    // Since the dijkstra is used to calculate the distance from the 'detectives' to mrX,
    // the edge which is only used by FERRY is excluded
    // All the incident edges have weight 1
    private void makeEdges(Board board) {
        GameSetup setup = board.getSetup();
        for(int i = 1; i < MAX + 1; i++) {
            for(int j = 1; j < MAX + 1; j++) {
                if(setup.graph.edgeValue(i,j).isPresent()) {
                    if(!setup.graph.edgeValue(i,j).equals(Optional.of(
                            ImmutableSet.of(ScotlandYard.Transport.FERRY)))) {
                        edges[i][j] = 1;
                        edges[j][i] = 1;
                    }
                }
            }
        }
    }


    // Helper function to return an arraylist of adjacent node
    // Cannot use graph.adjacentNode since this includes ferry as well
    // which detectives cannot use
    private List<Node> adjacentForDetective(int source) {
        ArrayList<Node> adjacentNode = new ArrayList<>();
        for (int i = 1; i < MAX + 1; i++) {
            if(edges[source][i] == 1) adjacentNode.add(shortestP[i]);
        }
        return adjacentNode;
    }

    private void dijkstra() {
        for(int i = 1; i < MAX + 1; i++) {
            if((edges[source][i] == 1) || (edges[i][source] == 1)) {
                shortestP[i].distance = 1;
            }
        }
        visited[source] = true;
        pq.offer(shortestP[source]);
        while(!pq.isEmpty()) {
            Node n = pq.poll();
            for(Node node : adjacentForDetective(n.node)) {
                visited[node.node] = true;
                update(node.node);
            }
        }
    }

    private boolean hasTicket(ScotlandYard.Transport t) {
        ScotlandYard.Ticket ticketUsed;
        if(t.toString().equals(ScotlandYard.Ticket.TAXI.toString())) ticketUsed = ScotlandYard.Ticket.TAXI;
        else if(t.toString().equals(ScotlandYard.Ticket.BUS.toString())) ticketUsed = ScotlandYard.Ticket.BUS;
        else ticketUsed = ScotlandYard.Ticket.UNDERGROUND;
        return (this.ticketMap.get(ticketUsed) != null);
    }


    private void useTickets(ScotlandYard.Transport t) {
        Map<ScotlandYard.Ticket, Integer> newTickets = new HashMap<>(this.ticketMap);
        ScotlandYard.Ticket ticketUsed;
        if(t.toString().equals(ScotlandYard.Ticket.TAXI.toString())) ticketUsed = ScotlandYard.Ticket.TAXI;
        else if(t.toString().equals(ScotlandYard.Ticket.BUS.toString())) ticketUsed = ScotlandYard.Ticket.BUS;
        else ticketUsed = ScotlandYard.Ticket.UNDERGROUND;
        newTickets.computeIfPresent(ticketUsed, (ticket, n) -> n - 1);
        this.ticketMap = newTickets;
    }

    private void update(int current) {
        for( Node adj : adjacentForDetective(current)) {
            int adjacent = adj.node;
            ImmutableSet<ScotlandYard.Transport> transport =
                    board.getSetup().graph.edgeValue(adjacent, current).orElseThrow();
            Iterator<ScotlandYard.Transport> it = transport.iterator();
            ScotlandYard.Transport trans = it.next();
            if((shortestP[adjacent].distance > shortestP[current].distance + edges[current][adjacent]) &&
                    (edges[current][adjacent] > 0) && hasTicket(trans)) {
                shortestP[adjacent].distance = shortestP[current].distance + edges[current][adjacent];
                useTickets(trans);
                if (!visited[adjacent] ) {
                    pq.offer(shortestP[adjacent]);
                }
                else {
                    pq.remove(shortestP[adjacent]);
                    pq.offer(shortestP[adjacent]);
                }
            }
        }
    }


    public int printWeight(int destination) {
        return shortestP[destination].distance;
    }


}
