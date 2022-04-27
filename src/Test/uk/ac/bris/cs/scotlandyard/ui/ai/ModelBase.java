package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import com.google.common.io.Resources;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.GameSetup;
import uk.ac.bris.cs.scotlandyard.model.MyGameStateFactory;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.readGraph;

public abstract class ModelBase {

    public static MyGameStateFactory myGameState;

    public static ImmutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> graph;

    @BeforeClass
    public static MyGameStateFactory getState() {
        myGameState  = new MyGameStateFactory();
        return myGameState;
    }

    @BeforeClass
    public static ImmutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> getGraph() throws IOException {
        graph = ScotlandYard.standardGraph();
        return graph;
    }

    @Nonnull
    public static GameSetup  getSetup() {
        return new GameSetup(graph, ScotlandYard.STANDARD24MOVES);
    }

    @Nonnull
    public static ImmutableMap<ScotlandYard.Ticket, Integer> makeTickets(int taxi, int bus,
                                                                         int underground, int db, int secret) {
        Map<ScotlandYard.Ticket, Integer> ticket = new HashMap<>();
        ticket.put(ScotlandYard.Ticket.TAXI, taxi);
        ticket.put(ScotlandYard.Ticket.BUS, bus);
        ticket.put(ScotlandYard.Ticket.UNDERGROUND, underground);
        ticket.put(ScotlandYard.Ticket.DOUBLE, db);
        ticket.put(ScotlandYard.Ticket.SECRET, secret);
        return ImmutableMap.copyOf(ticket);
    }
}
