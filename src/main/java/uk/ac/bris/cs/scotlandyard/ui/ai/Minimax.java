package uk.ac.bris.cs.scotlandyard.ui.ai;
import uk.ac.bris.cs.scotlandyard.model.*;

import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.*;
import static uk.ac.bris.cs.scotlandyard.model.Piece.MrX.MRX;
import static uk.ac.bris.cs.scotlandyard.model.Move.*;



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
    DijkstraCache dijkstraCache;
    private int maxDepth;

    Minimax(Board.GameState gameState, int steps, int mrXInitialLocation, Long maxTime){
        this.gameState = gameState;
        this.steps = steps;
        this.mrXInitialLocation = mrXInitialLocation;
        this.maxTime = maxTime * 1000;
        this.startTime = System.currentTimeMillis();
        this.dijkstraCache = new DijkstraCache();
    }

    private int searchBestScore(Board.GameState state, int depth, int alpha, int beta, boolean isMrX, int mrXLocation) {
        // Stop searching if the depth is zero, there's a winner or the time's nearly up
        if (depth == 0 || !state.getWinner().isEmpty() || (System.currentTimeMillis() - startTime > maxTime - 2000))
            return score(state, mrXLocation);
        maxDepth = Math.max(maxDepth, steps - depth + 1);
        if (isMrX) {
            int maxEval = minusInfinity;
            ArrayList<Move> movesToCheck = movesFilter(state, mrXLocation);

            // Pick one of the "best" moves to investigate first
            // Moves which increase distance tend to be better

            movesToCheck = ArrayList.sort(
                    movesToCheck,
                    (x, y) -> leftMovesFurther(state, x, y, mrXLocation)
            );

            for (Move currMove : movesToCheck)
                if (currMove.visit(new FunctionalVisitor<>(m -> true, m -> true))) {
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
                        if(depth == steps)
                        bestMove = currMove;
                    }
                    alpha = Math.max(alpha, eval);
                    if (beta <= alpha) break;
                }
            //System.out.println("Max score: " + maxEval);
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

    private boolean leftMovesFurther(Board.GameState state, Move x, Move y, int mrXLocation) {
        return getDistToMrX(state.advance(x), mrXLocation) > getDistToMrX(state.advance(y), mrXLocation);
    }

    private int getDistToMrX(Board.GameState state, int mrXLocation) {
        return dijkstraCache.getDistance(state, getDetectiveLocations(state), mrXLocation);
    }

    private int score(Board.GameState state, int mrXLocation){
        int distanceToMrX = dijkstraCache.getDistance(state, getDetectiveLocations(state), mrXLocation);

        return 100 * distanceToMrX + state.getAvailableMoves().size() + 2 * ticketFactor(state);
    }

    public Move getBestMove(){
        searchBestScore(gameState, steps, minusInfinity, plusInfinity, true, mrXInitialLocation);
        System.out.println("Number of verified moves: " + verifiedMoves);
        System.out.println("Looking " + maxDepth + " steps ahead");
        System.out.println("Size of Dijkstra Cache is: " + dijkstraCache.getSize());
        System.out.printf("Time elapsed: %.3f seconds%n", ((System.currentTimeMillis() - startTime) / (float)1000));

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
        if(new Dijkstra(state.getSetup().graph, getDetectiveLocations(state),mrXLocation).getDistTo() > 2){
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
                + tickets.get().getCount(BUS) * 3
                + tickets.get().getCount(UNDERGROUND) * 4
                + tickets.get().getCount(SECRET) * 8
                + tickets.get().getCount(DOUBLE) * 12;
    }
}
