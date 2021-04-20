package uk.ac.bris.cs.scotlandyard.ui.ai;
import uk.ac.bris.cs.scotlandyard.model.*;

import static uk.ac.bris.cs.scotlandyard.model.Piece.MrX.MRX;
import static uk.ac.bris.cs.scotlandyard.model.Move.*;



import java.util.*;

public class Minimax {

    int steps;
    final int minusInfinity = -1000000;
    final int plusInfinity = +1000000;
    Move bestMove;
    int verifiedMoves = 0;
    Long maxTime;
    Long startTime;
    DijkstraCache dijkstraCache;
    private int maxDepth;

    Minimax() {
        this.dijkstraCache = new DijkstraCache();
        this.startTime = System.currentTimeMillis();

    }

    private int searchBestScore(Board.GameState state, int depth, int alpha, int beta, boolean isMrX, int mrXLocation, int mrXAvailableMovesCount) {
        // Stop searching if the depth is zero, there's a winner or the time's nearly up

        if (depth == 0)
            return score(state, mrXLocation, mrXAvailableMovesCount);
        if (!state.getWinner().isEmpty())
            return score(state, mrXLocation, mrXAvailableMovesCount);
        // If the time elapsed (ms) is larger than the time-limit (minus a buffer), start exiting
        // The current buffer is 2 SECONDS - best to tweak when testing so it doesn't take forever
        if ((System.currentTimeMillis() - startTime > (maxTime - 20) * 1000))
            return score(state, mrXLocation, mrXAvailableMovesCount);


        maxDepth = Math.max(maxDepth, steps - depth + 1);
        if (isMrX) {
            mrXAvailableMovesCount = state.getAvailableMoves().size();
            int maxEval = minusInfinity;
            ArrayList<Move> movesToCheck = filterMrXMoves(state, mrXLocation);

            for (Move currMove : movesToCheck)
                if (currMove.visit(new FunctionalVisitor<>(m -> true, m -> true))) {
                    verifiedMoves++;
                    int eval = searchBestScore(
                            state.advance(currMove),
                            depth - 1,
                            alpha,
                            beta,
                            false,
                            getDest(currMove),
                            mrXAvailableMovesCount);

                    if (maxEval < eval) {
                        maxEval = eval;
                        if (depth == steps)
                            bestMove = currMove;
                    }
                    alpha = Math.max(alpha, eval);
                    if (beta <= alpha) break;
                }
            //System.out.println("Max score: " + maxEval);
            return maxEval;

        } else {
            boolean isLastDetective = checkIfLastDetective(state);
            int changeDepth = 0;
            if (isLastDetective) changeDepth = 1;

            int minEval = plusInfinity;

            ArrayList<Move> movesToCheck = filterDetectiveMoves(state, mrXLocation);
            for (Move currMove : movesToCheck) {
                verifiedMoves++;
                int eval = searchBestScore(
                        state.advance(currMove),
                        depth,
                        alpha,
                        beta,
                        isLastDetective,
                        mrXLocation,
                        mrXAvailableMovesCount);

                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) break;
            }
            return minEval;
        }
    }

    private int score(Board.GameState state, int mrXLocation, int mrXAvailableMovesCount) {
        int distanceToMrX = dijkstraCache.getDistance(state, getDetectiveLocations(state), mrXLocation);

        // Avoid positions where mrX will be caught
        if (distanceToMrX == 1) return minusInfinity;
        //System.out.println("Distance to Mr X: " + distanceToMrX);

        return 10 * distanceFactor(distanceToMrX) +
                5 * mrXAvailableMovesCount +
                ticketFactor(state) +
                locationsFactor(state);
    }

    public int locationsFactor(Board.GameState state) {
        PossibleLocations p = new PossibleLocations(state.getMrXTravelLog());
        p.getPossibleLocations();
        return 0;
    }

    // Returns a score based on the distance Moriarty is from the detectives
    // Increasing distance isn't as good when you're already far away
    public int distanceFactor(int distanceToMrX) {
        return (int) Math.round(Math.log(distanceToMrX+1)/Math.log(2));
    }

    //Return a score based on the tickets Moriarty currently has
    public int ticketFactor(Board.GameState state) {
        Optional<Board.TicketBoard> tickets = state.getPlayerTickets(MRX);

        double[] multipliers =
                //TAXI, BUS, UNDERGROUND, SECRET, DOUBLE
                {  1  ,  2 ,     4      ,  10   ,   12  };

                // A double is worth slightly more than one unit distance
                // Hopefully, this means it's only used to improve other factors

        int score = 0;
        for (int i = 0; i < 5; i++) {
            int num = tickets.get().getCount(ScotlandYard.Ticket.values()[i]);
            // A hefty penalty is applied when MrX runs out of a ticket type
            if (num == 0) score -= 50;
            else score += multipliers[i] * num;
        }

        return score;
    }

    public Move getBestMove(Board.GameState state, int steps, int mrXLocation, Long maxTime) {
        this.maxTime = maxTime;
        this.steps = steps;

        searchBestScore(state, steps, minusInfinity, plusInfinity, true, mrXLocation, 0);
        System.out.println("MrX's location is: " + getDest(bestMove));
        System.out.println("Minimum distance to MrX is: " + dijkstraCache.getDistance(state, getDetectiveLocations(state), getDest(bestMove)));
        System.out.println("Number of verified moves: " + verifiedMoves);
        System.out.println("Looking " + maxDepth + " steps ahead");
        System.out.println("Size of Dijkstra Cache is: " + dijkstraCache.getSize());
        System.out.printf("Time elapsed: %.3f seconds%n", ((System.currentTimeMillis() - startTime) / (float) 1000));
        System.out.printf("Score of chosen move: " + score(state, getDest(bestMove), 0) + "%n");

        return bestMove;
    }

    private int getDest(Move move) {
        return move.visit(new Move.FunctionalVisitor<>(m -> m.destination, m -> m.destination2));
    }

    // Checks the available moves of the state to see if there are other players left to make a move in the current round
    private boolean checkIfLastDetective(Board.GameState state) {
        ArrayList<Move> moves = new ArrayList<>(state.getAvailableMoves().asList());
        if (moves.isEmpty()) return true;
        Piece firstPiece = moves.get(0).commencedBy();
        for (Move move : moves) {
            if (move.commencedBy() != firstPiece) return false;
        }
        return true;
    }

    private ArrayList<Move> filterMrXMoves(Board.GameState state, int mrXLocation) {
        ArrayList<Move> allMoves = new ArrayList<>(state.getAvailableMoves().asList());
        ArrayList<Move> movesToCheck = new ArrayList<>();

        // Omit doubleMoves if mrX isn't close to being caught (detective more than 2 nodes away)
        if (new Dijkstra(state.getSetup().graph, getDetectiveLocations(state), mrXLocation, true).getDistToDestination() > 2) {
            for (Move move : allMoves) {
                boolean isSingleMove = move.visit(new Move.FunctionalVisitor<>(m -> true, m -> false));
                if (isSingleMove)
                    movesToCheck.add(move);
            }
            return movesToCheck;
        }
        return allMoves;
    }

    private ArrayList<Move> filterDetectiveMoves(Board.GameState state, int mrXLocation) {

        ArrayList<Move> allMoves = new ArrayList<>(state.getAvailableMoves().asList());
        Piece currPiece = allMoves.get(0).commencedBy();
        Integer detectiveLocation = allMoves.get(0).source();

        ArrayList<Integer> distances = new Dijkstra(state.getSetup().graph, new ArrayList<>(Arrays.asList(mrXLocation)), detectiveLocation, false).getDistances();

        ArrayList<Move> currDetectiveMoves = new ArrayList<>();
        for (Move move : allMoves)
            if (move.commencedBy() == currPiece)
                currDetectiveMoves.add(move);

        // Pick one of the "best" moves to investigate first
        // Moves which DECREASE distance tend to be better
        Collections.sort(currDetectiveMoves, Comparator.comparingInt(move -> distances.get(getDest(move))));

        ArrayList<Move> movesToCheck = new ArrayList<>();
        for (Move move : currDetectiveMoves)
            if (distances.get(getDest(move)).equals(distances.get(getDest(currDetectiveMoves.get(0)))))
                movesToCheck.add(move);

        return movesToCheck;

    }

    private ArrayList<Integer> getDetectiveLocations(Board.GameState state) {
        ArrayList<Integer> detectiveLocations = new ArrayList<>();
        ArrayList<Piece> pieces = new ArrayList<>(state.getPlayers());
        for (Piece piece : pieces)
            if (piece.isDetective()) {
                Optional<Integer> location = state.getDetectiveLocation((Piece.Detective) piece);
                location.ifPresent(detectiveLocations::add);
            }
        //Collections.sort(detectiveLocations);
        return detectiveLocations;

    }
}
