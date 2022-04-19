package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.graph.ImmutableValueGraph;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.GameSetup;



public class Dijkstra {

    int source;
    int destination;
    Board board;
    GameSetup setup;
    private int [] shortestP;
    private PriorityQueue pq;


    public Dijkstra (Board board, int source, int destination) {

        this.board = board;
        this.source = source;
        this.destination = destination;
        this.setup = board.getSetup();

        initialise(source);
        pq.push(source, shortestP[source - 1]);
        while(pq.size() != 0) {
            PriorityQueue.Distance d = pq.pop();
            for (int n :setup.graph.adjacentNodes(d.getNode())) {
                // TODO Currently found an error from update() function
                update(n, d.getNode());
            }
        }

    }

    // Initialises the main setup
    public void initialise(int s) {
        int BIG = 1000;
        int n = setup.graph.nodes().size();
        shortestP = new int[n];
        pq = new PriorityQueue(n);
        for (int i = 0; i < n; i++) shortestP[i] = BIG;
        shortestP[s-1] = 0;
        for (int node: setup.graph.adjacentNodes(source)) {
            // Adjacent nodes have weight 1
            shortestP[node-1] = 1;
        }
    }

    // Update the shortestP and PriorityQueue
    // Variable current refers to current vertex
    public void update(int node, int current) {;
        // Index for shortestP
        int index = node - 1;
        int currentIndex = current - 1;
        // Calculates by adding one because every adjacent node has value 1
        if (shortestP[index] > shortestP[currentIndex] + 1) {
            shortestP[index] = shortestP[currentIndex] + 1;
        }

        if(pq.contains(node)) pq.setDistance(node, shortestP[index]);
        else pq.push(node, shortestP[index]);
    }

    public int getDistance(int node) {
        return shortestP[node - 1];
    }
}
