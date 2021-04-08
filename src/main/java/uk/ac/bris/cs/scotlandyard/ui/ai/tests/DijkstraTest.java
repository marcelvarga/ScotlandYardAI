package uk.ac.bris.cs.scotlandyard.ui.ai.tests;

import uk.ac.bris.cs.scotlandyard.model.MyGameStateFactory;
import uk.ac.bris.cs.scotlandyard.model.Player;
import uk.ac.bris.cs.scotlandyard.ui.ai.Dijkstra;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;

import java.util.ArrayList;

import static uk.ac.bris.cs.scotlandyard.model.Piece.Detective.BLUE;
import static uk.ac.bris.cs.scotlandyard.model.Piece.Detective.GREEN;
import static uk.ac.bris.cs.scotlandyard.model.Piece.Detective.RED;
import static uk.ac.bris.cs.scotlandyard.model.Piece.Detective.WHITE;
import static uk.ac.bris.cs.scotlandyard.model.Piece.Detective.YELLOW;
import static uk.ac.bris.cs.scotlandyard.model.Piece.MrX.MRX;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.DETECTIVE_LOCATIONS;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.MRX_LOCATIONS;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.STANDARD24ROUNDS;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.BUS;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.DOUBLE;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.SECRET;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.TAXI;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.UNDERGROUND;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.defaultDetectiveTickets;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.defaultMrXTickets;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.readGraph;


class DijkstraTest {

    Player mrX = new Player(MRX, defaultMrXTickets(), 3);
    Player blue = new Player(BLUE, defaultDetectiveTickets(), 4);
    //GameState state = MyGameStateFactory.build(standard24RoundSetup(), mrX, blue);

    @org.junit.jupiter.api.Test
    public void testDijkstraCreation() throws Exception {
        try {
            //Dijkstra d = new Dijkstra();
        } catch (Exception e) {
            throw new Exception("Dijkstra could not be created");
        }
    }

    @org.junit.jupiter.api.Test
    public void testObviousDijkstra() throws Exception {
        try {
            //Dijkstra d = new Dijkstra();
        } catch (Exception e) {
            throw new Exception("Dijkstra was very stupid");
        }
    }
}