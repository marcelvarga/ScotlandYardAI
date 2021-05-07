package uk.ac.bris.cs.scotlandyard.ui.ai;
import uk.ac.bris.cs.scotlandyard.model.*;

import static uk.ac.bris.cs.scotlandyard.model.Piece.MrX.MRX;
import static uk.ac.bris.cs.scotlandyard.model.Move.*;

import java.util.*;

@SuppressWarnings("UnstableApiUsage")

public class Minimax {

    int steps;
    final int minusInfinity = -1000000;
    final int plusInfinity = +1000000;
    Move bestMove;
    int verifiedMoves = 0;
    Long maxTime;
    Long startTime;
    DijkstraCache dijkstraCache;
    boolean mrXIsCaller;
    private int maxDepth;

    Minimax() {
        this.dijkstraCache = new DijkstraCache();
        this.startTime = System.currentTimeMillis();
    }

    public int searchBestScore(Situation situation, int depth, int alpha, int beta, boolean isMrX, int mrXLocation, int mrXAvailableMovesCount) {
        // Stop searching if the depth is zero, there's a winner or the time's nearly up
        if (depth == 0) return score(situation, mrXLocation, mrXAvailableMovesCount);
        if (!situation.getWinner().isEmpty()) return score(situation, mrXLocation, mrXAvailableMovesCount);
        // If the time elapsed (ms) is larger than the time-limit (minus a buffer), start exiting

        // The current buffer is 5 SECONDS - best to tweak when testing so it doesn't take forever
        if ((System.currentTimeMillis() - startTime > (maxTime - 5) * 1000)){
            System.out.println("RAN OUT OF TIME");
            return score(situation, mrXLocation, mrXAvailableMovesCount);}


        maxDepth = Math.max(maxDepth, steps - depth + 1);
        if (isMrX) {
            mrXAvailableMovesCount = situation.getAvailableMoves().size();
            int maxEval = minusInfinity;
            ArrayList<Move> movesToCheck = filterMrXMoves(situation, mrXLocation);

            for (Move currMove : movesToCheck)
                if (currMove.visit(new FunctionalVisitor<>(m -> true, m -> true))) {
                    verifiedMoves++;
                    int eval = searchBestScore(
                            situation.advance(currMove),
                            depth - 1,
                            alpha,
                            beta,
                            false,
                            getDest(currMove),
                            mrXAvailableMovesCount);

                    if (maxEval < eval) {
                        maxEval = eval;
                        if (depth == steps && mrXIsCaller)
                            bestMove = currMove;
                    }
                    alpha = Math.max(alpha, eval);
                    if (beta <= alpha) break;
                }
            //System.out.println("Max score: " + maxEval);
            return maxEval;

        } else {
            boolean isLastDetective = checkIfLastDetective(situation);

            int minEval = plusInfinity;

            ArrayList<Move> movesToCheck = filterDetectiveMoves(situation, mrXLocation);
            for (Move currMove : movesToCheck) {
                verifiedMoves++;
                int eval = searchBestScore(
                        situation.advance(currMove),
                        depth,
                        alpha,
                        beta,
                        isLastDetective,
                        mrXLocation,
                        mrXAvailableMovesCount);

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

    public int score(Situation situation, int mrXLocation, int mrXAvailableMovesCount) {
        int distanceToMrX = dijkstraCache.getDistance(situation.getState(), getDetectiveLocations(situation), mrXLocation);
        /*System.out.println("Distance factor is: " + 50 * distanceFactor(distanceToMrX));
        System.out.println("MrXMoves factor is: " + 0.5 * mrXAvailableMovesCount);
        System.out.println("Ticket factor is: " + 0.1 * ticketFactor(situation));
        System.out.println("Location factor is: " + 5 * Math.pow(situation.numPossibleLocations(), 0.5));*/
        return (int) (
                50 * distanceFactor(distanceToMrX) +
                0.5 * mrXAvailableMovesCount +
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
                {  1  ,  2 ,     4      ,  10   ,   12  };

                // A double is worth slightly more than one unit distance
                // Hopefully, this means it's only used to improve other factors

        int score = 0;
        for (int i = 0; i < 5; i++) {
            assert tickets.orElse(null) != null;
            int num = tickets.orElse(null).getCount(ScotlandYard.Ticket.values()[i]);
            // A hefty penalty is applied when MrX runs out of a ticket type
            if (num == 0) score -= 50;
            else score += multipliers[i] * num;
        }

        return score;
    }

    public Move getBestMove(Situation situation, int steps, int mrXLocation, Long maxTime, boolean mrXIsCaller) {
        this.maxTime = maxTime;
        this.steps = steps;
        this.mrXIsCaller = mrXIsCaller;
        searchBestScore(situation, steps, minusInfinity, plusInfinity, mrXIsCaller, mrXLocation, 0);
        /*System.out.println("MrX's location is: " + getDest(bestMove));
        System.out.println("Minimum distance to MrX is: " + dijkstraCache.getDistance(situation.getState(), getDetectiveLocations(situation), getDest(bestMove)));
        System.out.println("Number of verified moves: " + verifiedMoves);
        System.out.println("Looking " + maxDepth + " steps ahead");
        System.out.println("Size of Dijkstra Cache is: " + dijkstraCache.getSize());
        System.out.printf("Time elapsed: %.3f seconds%n", ((System.currentTimeMillis() - startTime) / (float) 1000));*/

        /*int distanceToMrX = dijkstraCache.getDistance(situation.getState(), getDetectiveLocations(situation), mrXLocation);
        int mrXAvailableMovesCount = situation.getAvailableMoves().size();
        System.out.println("\n------------Score breakdown------------");
        System.out.println("Best Move: " + bestMove.toString());
        System.out.printf("Score of chosen move: " + score(situation, getDest(bestMove), mrXAvailableMovesCount) + "%n");
        System.out.println("Distance factor: " + 50 * distanceFactor(distanceToMrX));
        System.out.printf("MrXMoves factor: %.2f\n", 0.5 * mrXAvailableMovesCount);
        System.out.printf("Ticket factor: %.2f\n", 0.1 * ticketFactor(situation));
        System.out.printf("Location factor: %.2f\n", 10 * Math.pow(situation.numPossibleLocations(), 0.7));
        System.out.println("MrX could be at:");
        for (Integer location : situation.possibleLocations()) {
            System.out.println(" - " + location);
        }*/
        return bestMove;
    }

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

    private ArrayList<Move> filterMrXMoves(Situation situation, int mrXLocation) {
        ArrayList<Move> temp0 = new ArrayList<>(situation.getAvailableMoves().asList());
        ArrayList<Move> temp1 = new ArrayList<>(temp0);

        Dijkstra d = new Dijkstra(situation.getState().getSetup().graph, getDetectiveLocations(situation), mrXLocation, true);


        FunctionalVisitor<Boolean> isDoubleMoveVisitor = new FunctionalVisitor<>(m -> false, m -> true);
        // Remove double moves if no detective is closer than 2 moves away from MrX
        temp0.removeIf(m -> ((d.getDistances().get(getDest(m)) > 2) && (m.visit(isDoubleMoveVisitor))));
        if(temp0.isEmpty()) temp0.addAll(temp1);
            else temp1.clear(); temp1.addAll(temp0);
        // Remove moves that would get MrX immediately caught
        temp0.removeIf(m -> d.getDistances().get(getDest(m)) == 1);
        if(temp0.isEmpty()) temp0.addAll(temp1);
            else temp1.clear(); temp1.addAll(temp0);

        FunctionalVisitor<Boolean> isAnyTicketSecret = new FunctionalVisitor<>(
                m -> m.ticket == ScotlandYard.Ticket.SECRET,
                m -> (m.ticket1 == ScotlandYard.Ticket.SECRET || m.ticket2 == ScotlandYard.Ticket.SECRET)
        );

        // Remove moves that effectively waste secret tickets
        if(situation.isRevealTurnNext()){ temp0.removeIf(m -> m.visit(isAnyTicketSecret));

        if(temp0.isEmpty()) temp0.addAll(temp1);
            else temp1.clear(); temp1.addAll(temp0);
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
        if(temp0.isEmpty()) temp0.addAll(temp1);
            else temp1.clear(); temp1.addAll(temp0);

        // Remove moves that reduce MrX's possible locations to 1 UNLESS he's going into a reveal turn
        // Even though this seems bad, using doubles on the first two reveal turns might be his comeuppance later on
        // This is quite processing heavy, so only run if MrX is in a pickle (only 5 possibleLocations)
        if (situation.numPossibleLocations() < 6) {
            temp0.removeIf(m -> (situation.advance(m).numPossibleLocations() == 1) && !situation.advance(m).getIsRevealTurn());
            if(temp0.isEmpty()) temp0.addAll(temp1);
        }


        temp0.sort(Comparator.comparingInt(move -> -d.getDistances().get(getDest(move))));
        return temp0;

        /*if (!temp.isEmpty()) {
            temp.sort(Comparator.comparingInt(move -> -d.getDistances().get(getDest(move))));
            return temp;
        }
        return allMoves;*/
    }

    private boolean sameDestDoubleMoves(Move m1, Move m2){
        if(getDest(m1) != getDest(m2)) return false;
        return m1.tickets().equals(m2.tickets());
    }
    private ArrayList<Move> filterDetectiveMoves(Situation situation, int mrXLocation) {

        ArrayList<Move> allMoves = new ArrayList<>(situation.getAvailableMoves().asList());
        Piece currPiece = allMoves.get(0).commencedBy();
        Integer detectiveLocation = allMoves.get(0).source();

        ArrayList<Integer> distances = new Dijkstra(situation.getState().getSetup().graph, new ArrayList<>(Collections.singletonList(mrXLocation)), detectiveLocation, false).getDistances();
        allMoves.removeIf(m -> !(m.commencedBy().equals(currPiece)));

        // Pick one of the "best" moves to investigate first
        // Moves which DECREASE distance tend to be better
        allMoves.sort(Comparator.comparingInt(move -> distances.get(getDest(move))));

        allMoves.removeIf(m -> (getDest(m) != getDest(allMoves.get(0))));

        // Remove moves that, if the detective can land on a possible location, don't
        // TODO
        if (allMoves.stream().anyMatch(m -> situation.possibleLocations().contains(getDest(m)))) {
            allMoves.removeIf(m -> !situation.possibleLocations().contains(getDest(m)));
        }

        return allMoves;

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
}
