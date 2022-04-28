import com.google.common.collect.ImmutableMap;
import uk.ac.bris.cs.scotlandyard.model.GameSetup;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.MyGameStateFactory;
import uk.ac.bris.cs.scotlandyard.model.Piece;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.*;

public abstract class TestModel {

    public static void test(boolean b) {
        if(!b) throw new AssertionError("Incorrect");
    }

    public static ImmutableMap<Ticket, Integer> makeTicketsManual(
            int taxi, int bus, int underground, int db, int secret) {
        Map<Ticket, Integer> tickets = new HashMap<>();
        tickets.put(TAXI, taxi);
        tickets.put(BUS, bus);
        tickets.put(UNDERGROUND, underground);
        tickets.put(DOUBLE, db);
        tickets.put(SECRET, secret);
        return ImmutableMap.copyOf(tickets);
    }

    public static MyGameStateFactory createGameState() {
        return new MyGameStateFactory();
    }

    public static GameSetup movesSetup() throws IOException {
        return new GameSetup(standardGraph(), STANDARD24MOVES);
    }

}
