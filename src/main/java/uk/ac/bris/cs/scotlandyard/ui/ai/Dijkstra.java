package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.GameSetup;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;

public class Dijkstra {

    private final Board board;
    // Maximum nodes
    private final int MAX;
    private final int source;
    private int[][] edges;
    private boolean[] visited;
    private Node [] shortestP;
    private PriorityQueue<Node> pq;

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

    public Dijkstra(Board board, int source) {
        this.board = board;
        this.MAX = board.getSetup().graph.nodes().size();
        this.source = source;
        initialise();
        dijkstra();
    }

    // Initialises the basic dijkstra class
    private void initialise() {
        edges = new int[MAX+1][MAX+1];
        shortestP = new Node[MAX+1];
        visited = new boolean[MAX+1];
        pq = new PriorityQueue<>();

        for(int i = 1; i < MAX+1; i++) {
            for(int j = 1; j < MAX+1; j++) {
                if(i == j) edges[i][j] = 0;
                // TODO An error here
                else edges[i][j] = -1;
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
                    if(!setup.graph.edgeValue(i,j).equals(ScotlandYard.Transport.FERRY)) {
                        edges[i][j] = 1;
                        edges[j][i] = 1;
                    }
                }
            }
        }
    }

    // Updates the status if the node is visited for the first time
    private void beenToNewPlace(int node) {
        pq.offer(shortestP[node]);
        visited[node] = true;
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
                shortestP[i].distance = edges[source][i];
            }
        }
        beenToNewPlace(source);
        while(!pq.isEmpty()) {
            Node n = pq.poll();
            for( Node node : adjacentForDetective(n.node)) {
                update(node.node);
            }
        }
    }

    private void update(int current) {
        for( Node adj : adjacentForDetective(current)) {
            int adjacent = adj.node;
            if((shortestP[adjacent].distance > shortestP[current].distance + edges[current][adjacent]) &&
                    (edges[current][adjacent] >= 0)) {
                shortestP[adjacent].distance = shortestP[current].distance + edges[current][adjacent];
                if (!visited[adjacent]) {
                    beenToNewPlace(adjacent);
                }
                else {
                    Node n = shortestP[adjacent];
                    pq.offer(n);
                }
            }
        }
    }

    public int printWeight(int destination) {
        return shortestP[destination].distance;
    }


}
