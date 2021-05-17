package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Piece;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;

import java.util.*;

import static uk.ac.bris.cs.scotlandyard.model.Move.FunctionalVisitor;
import static uk.ac.bris.cs.scotlandyard.model.Piece.MrX.MRX;

@SuppressWarnings("UnstableApiUsage")

public class Minimax {

    private int steps;
    private final int minusInfinity = -10000000;
    private final int plusInfinity = +10000000;
    private Move bestMove;
    private final long maxTime;
    private final long startTime;
    private final DijkstraCache dijkstraCache;
    boolean mrXIsCaller;

    Minimax(long maxTime) {
        this.dijkstraCache = new DijkstraCache();
        this.startTime = System.currentTimeMillis();
        this.maxTime = maxTime;
    }

    public Move getBestMove(Situation situation, int steps, int mrXLocation, boolean mrXIsCaller) {
        this.steps = steps;
        this.mrXIsCaller = mrXIsCaller;
        searchBestScore(situation, steps, minusInfinity, plusInfinity, mrXIsCaller, mrXLocation);
        return bestMove;
    }

    public int searchBestScore(Situation situation, int depth, int alpha, int beta, boolean isMrX, int mrXLocation) {
        // Stop searching if the depth is zero, there's a winner or the time's nearly up
        // If the time elapsed (ms) is larger than the time-limit (minus a buffer), start exiting
        if (depth == 0 ||
                !situation.getWinner().isEmpty() ||
                (System.currentTimeMillis() - startTime > (maxTime - 3) * 1000))
                return score(situation, mrXLocation);

        if (isMrX) {
            int maxEval = minusInfinity;
            ArrayList<Move> movesToCheck = filterMrXMoves(situation, mrXLocation);

            for (Move currMove : movesToCheck)
                if (currMove.visit(new FunctionalVisitor<>(m -> true, m -> true))) {
                    int eval = searchBestScore(
                            situation.advance(currMove),
                            depth - 1,
                            alpha,
                            beta,
                            false,
                            getDest(currMove));

                    if (maxEval < eval) {
                        maxEval = eval;
                        if (depth == steps && mrXIsCaller)
                            bestMove = currMove;
                    }
                    alpha = Math.max(alpha, eval);
                    if (beta <= alpha) break;
                }
            return maxEval;

        } else {
            boolean isLastDetective = checkIfLastDetective(situation);

            int minEval = plusInfinity;

            ArrayList<Move> movesToCheck = filterDetectiveMoves(situation, mrXLocation);
            for (Move currMove : movesToCheck) {
                int eval = searchBestScore(
                        situation.advance(currMove),
                        depth,
                        alpha,
                        beta,
                        isLastDetective,
                        mrXLocation);

                if (minEval > eval) {
                    minEval = eval;
                    if (depth == steps && !mrXIsCaller)
                        bestMove = currMove;
                }
                beta = Math.min(beta, eval);
                if (beta <= alpha) break;
            }
            return minEval;
        }
    }

    public int score(Situation situation, int mrXLocation) {
        int distanceToMrX = dijkstraCache.getDistance(situation.getState(), getDetectiveLocations(situation), mrXLocation);
        return (int) (
                50 * distanceFactor(distanceToMrX) +
                0.5 * situation.getAvailableMoves().size() +
                0.1 * ticketFactor(situation) +
                10 * Math.pow(situation.numPossibleLocations(), 0.7) +

                //Apply massive penalty if MrX could be caught
                ((distanceToMrX == 1) ? minusInfinity: 0));
    }

    // Returns a score based on the distance Moriarty is from the detectives
    // Increasing distance isn't as good when you're already far away
    private int distanceFactor(int distanceToMrX) {
        return (int) Math.round(Math.log(distanceToMrX+1)/Math.log(2));
    }

    //Return a score based on the tickets Moriarty currently has
    private int ticketFactor(Situation situation) {
        Optional<Board.TicketBoard> tickets = situation.getState().getPlayerTickets(MRX);

        double[] multipliers =
                //TAXI, BUS, UNDERGROUND, SECRET, DOUBLE
                {  1  ,  2 ,      4     ,  20   ,   20  };

        int score = 0;

        for (int i = 0; i < 5; i++) {
            //noinspection ConstantConditions
            int num = tickets.orElse(null).getCount(ScotlandYard.Ticket.values()[i]);

            // A hefty penalty is applied when MrX runs out of a ticket type
            if(score == 1) score -= 15 * multipliers[i];
            else if(score == 0) score -= 25 * multipliers[i];

            // SECRET is 3, DOUBLE is 4
            if(i == 3 || i == 4) score += num * multipliers[i];

        }

        return score;
    }

    // Helper functions //
    private int getDest(Move move) {
        return move.visit(new Move.FunctionalVisitor<>(m -> m.destination, m -> m.destination2));
    }
    // Checks the available moves of the state to see if there are other players left to make a move in the current round
    private boolean checkIfLastDetective(Situation situation) {
        ArrayList<Move> moves = new ArrayList<>(situation.getAvailableMoves().asList());
        if (moves.isEmpty()) return true;
        Piece firstPiece = moves.get(0).commencedBy();
        for (Move move : moves) {
            if (move.commencedBy() != firstPiece) return false;
        }
        return true;
    }
    private ArrayList<Integer> getDetectiveLocations(Situation situation) {
        ArrayList<Integer> detectiveLocations = new ArrayList<>();
        ArrayList<Piece> pieces = new ArrayList<>(situation.getPlayers());
        for (Piece piece : pieces)
            if (piece.isDetective()) {
                Optional<Integer> location = situation.getDetectiveLocation((Piece.Detective) piece);
                location.ifPresent(detectiveLocations::add);
            }
        return detectiveLocations;
    }

    // Moves filtering //
    public ArrayList<Move> filterMrXMoves(Situation situation, int mrXLocation) {
        // temp0 is used to store the moves and to filter, temp1 is used to cache temp0 in case it is empty after a filter
        ArrayList<Move> temp0 = new ArrayList<>(situation.getAvailableMoves().asList());
        ArrayList<Move> temp1 = new ArrayList<>(temp0);
        FunctionalVisitor<Boolean> isDoubleMoveVisitor = new FunctionalVisitor<>(m -> false, m -> true);
        FunctionalVisitor<Boolean> isSecondTicketSecretVisitor = new FunctionalVisitor<>(m -> false, m -> m.ticket2 == ScotlandYard.Ticket.SECRET);

        Dijkstra d = new Dijkstra(situation.getState().getSetup().graph, getDetectiveLocations(situation), mrXLocation, false);

        int remainingRounds = situation.getState().getSetup().rounds.size() - situation.getState().getMrXTravelLog().size();

        // Decides how to filter moves based on how far away MrX is at the moment
        // If MrX is more than 4 distance away, filter to optimise possibleLocations
        // Otherwise, filter to optimise distance

        if (d.getDistToDestination() > 3) {
            // Remove DoubleMoves if there are many rounds left
            //noinspection ConstantConditions
            int doubleMovesCount = situation.getState().getPlayerTickets(MRX).orElse(null).getCount(ScotlandYard.Ticket.DOUBLE);
            if(remainingRounds > (doubleMovesCount / 2)) {
                temp0.removeIf(m -> (m.visit(isDoubleMoveVisitor)));
                checkListNotEmpty(temp0, temp1);
                }
            // Sort by possible locations then by distance from detectives
            temp0.sort(Comparator.comparingInt(move -> -situation.advance((Move) move).numPossibleLocations())
                    .thenComparingInt(move -> -d.getDistances().get(getDest((Move) move))));
            temp0.removeIf(m -> (situation.advance(m).numPossibleLocations() < situation.advance(temp0.get(0)).numPossibleLocations() - 4));
        } else {

            // Sort by distance
            temp0.sort(Comparator.comparingInt(move -> -d.getDistances().get(getDest(move))));

            // Remove double moves if no detective is closer than 2 moves away from MrX
            // or that would get MrX immediately caught
            temp0.removeIf(m -> ((d.getDistances().get(getDest(m)) > 2) && (m.visit(isDoubleMoveVisitor))));
            temp0.removeIf(m -> d.getDistances().get(getDest(m)) == 1);

        }
        checkListNotEmpty(temp0, temp1);


        if(situation.isRevealTurnNext()) {
            // Remove moves that reduce MrX's possible locations to 1
            temp0.removeIf(m -> ((situation.advance(m).numPossibleLocations() == 1) && !m.visit(isDoubleMoveVisitor)));
            checkListNotEmpty(temp0, temp1);

            // Remove moves that effectively waste secret tickets
            temp0.removeIf(m -> m.tickets().iterator().next() == ScotlandYard.Ticket.SECRET);
            checkListNotEmpty(temp0, temp1);
        }

        if(situation.isRevealTurnNextNext()) {
            // Remove moves that reduce MrX's possible locations to 1
            temp0.removeIf(m -> ((situation.advance(m).numPossibleLocations() == 1) && m.visit(isDoubleMoveVisitor)));
            checkListNotEmpty(temp0, temp1);

            // Remove moves that effectively waste secret tickets
            temp0.removeIf(m -> m.visit(isDoubleMoveVisitor) && m.visit(isSecondTicketSecretVisitor));
            checkListNotEmpty(temp0, temp1);
        }

        // Remove duplicate double moves that use the same tickets IN ORDER and end at the same location
        int len = temp0.size();
        for (int i = 0; i < len - 1; i++)
            for (int j = i + 1; j < len; j++)
                if(sameDestDoubleMoves(temp0.get(i), temp0.get(j))) {
                    temp0.remove(j);
                    j--;
                    len--;
                }
        checkListNotEmpty(temp0, temp1);

        return temp0;
    }

    // Filtering helpers //
    private void checkListNotEmpty(ArrayList<Move> list0, ArrayList<Move> list1){
        if (list0.isEmpty()) list0.addAll(list1);
            else list1.clear();
        list1.addAll(list0);
    }

    private boolean sameDestDoubleMoves(Move m1, Move m2){
        if(getDest(m1) != getDest(m2)) return false;
        return m1.tickets().equals(m2.tickets());
    }

    // Filter the moves checked for detectives in the minimax tree
    // The algorithm assumes that detectives do good moves
    private ArrayList<Move> filterDetectiveMoves(Situation situation, int mrXLocation) {

        ArrayList<Move> allMoves = new ArrayList<>(situation.getAvailableMoves().asList());
        Piece currPiece = allMoves.get(0).commencedBy();
        Integer detectiveLocation = allMoves.get(0).source();
        ArrayList<Integer> distances = new Dijkstra(situation.getState().getSetup().graph, new ArrayList<>(Collections.singletonList(mrXLocation)), detectiveLocation, false).getDistances();

        // Remove all moves made by pieces other than the first one in the list
        allMoves.removeIf(m -> !(m.commencedBy().equals(currPiece)));

        // Pick one of the "best" moves to investigate first
        // Moves which DECREASE distance tend to be better
        allMoves.sort(Comparator.comparingInt(move -> distances.get(getDest(move))));

        allMoves.removeIf(m -> (getDest(m) != getDest(allMoves.get(0))));

        // Remove moves that miss on the opportunity to land on one of MrX's possible locations
        if (allMoves.stream().anyMatch(m -> situation.getPossibleLocations().contains(getDest(m)))) {
            allMoves.removeIf(m -> !situation.getPossibleLocations().contains(getDest(m)));
        }

        return allMoves;
    }
}
