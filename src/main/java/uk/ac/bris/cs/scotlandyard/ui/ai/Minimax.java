package uk.ac.bris.cs.scotlandyard.ui.ai;
import uk.ac.bris.cs.scotlandyard.model.*;

import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.*;
import static uk.ac.bris.cs.scotlandyard.model.Piece.MrX.MRX;



import java.util.*;

public class Minimax {

    Board.GameState gameState;
    int steps;
    int mrXInitialLocation;
    final int minusInfinity = -1000000;
    final int plusInfinity  = +1000000;
    Move bestMove;
    int verifiedMoves = 0;
    Long maxTime;
    Long startTime;
    Map<ArrayList<Integer>, Integer> transpositionTable;
    private int newCalls;
    private int tableCalls;

    Minimax(Board.GameState gameState, int steps, int mrXInitialLocation, Long maxTime){
        this.gameState = gameState;
        this.steps = steps;
        this.mrXInitialLocation = mrXInitialLocation;
        this.maxTime = maxTime * 1000;
        this.startTime = System.currentTimeMillis();
        this.transpositionTable = new HashMap<>();
    }

    private int searchBestScore(Board.GameState state, int depth, int alpha, int beta, boolean isMrX, int mrXLocation) {
        if (depth == 0 || !state.getWinner().isEmpty())
            return score(state, mrXLocation);

        if (isMrX) {
            int maxEval = minusInfinity;
            ArrayList<Move> movesToCheck = movesFilter(state, mrXLocation);

            for (Move currMove : movesToCheck)
                if (currMove.visit(new Move.FunctionalVisitor<>(m -> true, m -> true))) {
                    verifiedMoves++;

                    int eval = searchBestScore(
                            state.advance(currMove),
                            depth - 1,
                            alpha,
                            beta,
                            false,
                            getDest(currMove));

                    if (maxEval < eval) {
                        maxEval = eval;
                        bestMove = currMove;
                    }
                    alpha = Math.max(alpha, eval);
                    if (beta <= alpha) break;
                }
            System.out.println("Max score: " + maxEval);
            return maxEval;

        } else {
            int minEval = plusInfinity;
            boolean isLastPlayer = checkIfLastPlayer(state);
            int changeDepth = 0;
            if (isLastPlayer) changeDepth = 1;
            ArrayList<Move> availableMoves = new ArrayList<>(state.getAvailableMoves());
            Piece currPiece = availableMoves.get(0).commencedBy();
            for (Move currMove : availableMoves)
                if (currMove.commencedBy() == currPiece) {
                    verifiedMoves++;

                    int eval = searchBestScore(
                            state.advance(currMove),
                            depth - changeDepth,
                            alpha,
                            beta,
                            isLastPlayer,
                            mrXLocation);

                    minEval = Math.min(minEval, eval);
                    beta = Math.min(beta, eval);
                    if (beta <= alpha) break;

                }
                return minEval;
        }
    }
    private int score(Board.GameState state, int mrXLocation){
        int distanceToMrX;
        ArrayList<Integer> detectiveLocations = getDetectiveLocations(state);
        ArrayList<Integer> playerLocations = new ArrayList<>(detectiveLocations);
        playerLocations.addAll(detectiveLocations);

        if (!transpositionTable.containsKey(playerLocations)) {
            newCalls++;
            transpositionTable.put(playerLocations, new Dijkstra(state.getSetup().graph, detectiveLocations, mrXLocation).getDistToMrX());
        }
        else
            tableCalls++;
        distanceToMrX = transpositionTable.get(playerLocations);


        return 10 * distanceToMrX + state.getAvailableMoves().size() + 2 * ticketFactor(state);
    }

    public Move getBestMove(){
        searchBestScore(gameState, steps, minusInfinity, plusInfinity, true, mrXInitialLocation);
        System.out.println(verifiedMoves);
        verifiedMoves = 0;
        System.out.println("Table calls " + tableCalls + ", Dijkstra calls " + newCalls);
        System.out.println("Size of Transposition Table is " + transpositionTable.size());
        System.out.println((System.currentTimeMillis() - startTime) / 1000);

        return bestMove;
    }

    private int getDest(Move move) {
        return move.visit(new Move.FunctionalVisitor<>(m -> m.destination, m -> m.destination2));
    }

    // Checks the available moves of the state to see if there are other players left to make a move
    // In the current round
    private boolean checkIfLastPlayer(Board.GameState state) {
        ArrayList<Move> moves = new ArrayList<>(state.getAvailableMoves().asList());
        if (moves.isEmpty()) return true;
        Piece firstPiece = moves.get(0).commencedBy();
        for (Move move : moves){
            if (move.commencedBy() != firstPiece) return false;
        }
        return true;
    }

    private ArrayList<Move> movesFilter(Board.GameState state, int mrXLocation){
        ArrayList<Move> allMoves = new ArrayList<>(state.getAvailableMoves().asList());
        ArrayList<Move> temp = new ArrayList<>();

        // Omit doubleMoves if mrX isn't close to being caught (detective more than 2 nodes away)
        if(new Dijkstra(state.getSetup().graph, getDetectiveLocations(state),mrXLocation) .getDistToMrX() > 2){
            for(Move move : allMoves){
                boolean isSingleMove = move.visit(new Move.FunctionalVisitor<>(m -> true, m -> false));
                if(isSingleMove)
                temp.add(move);
            }
            return temp;
        }
        return allMoves;
    }

    private ArrayList<Integer> getDetectiveLocations(Board.GameState state){
            ArrayList<Integer> detectiveLocations = new ArrayList<>();
            ArrayList<Piece> pieces = new ArrayList<>(state.getPlayers());
            for (Piece piece : pieces)
                if(piece.isDetective()){
                    Optional<Integer> location = state.getDetectiveLocation((Piece.Detective) piece);
                    location.ifPresent(detectiveLocations::add);
                }
            //Collections.sort(detectiveLocations);
            return detectiveLocations;

    }
    //Return a score based on the tickets Moriarty currently has

    //This removes problem of Moriarty using SECRET tickets unnecessarily
    public int ticketFactor(Board.GameState state) {
        Optional<Board.TicketBoard> tickets = state.getPlayerTickets(MRX);
        return tickets.get().getCount(TAXI)
                + tickets.get().getCount(BUS) * 2
                + tickets.get().getCount(UNDERGROUND) * 3
                + tickets.get().getCount(SECRET) * 5
                + tickets.get().getCount(DOUBLE) * 10;
    }
}
