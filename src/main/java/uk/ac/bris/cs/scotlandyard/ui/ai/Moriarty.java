package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.model.Board;

public class Moriarty implements Ai {

	@Nonnull @Override public String name() { return "Moriarty"; }

	@Nonnull @Override public Move pickMove(
			@Nonnull Board board,
			Pair<Long, TimeUnit> timeoutPair) {

		var moves = board.getAvailableMoves().asList();


		//bestNode is now the node with the most optimistic outcome
		//This doesn't make much sense, as the best outcome is the detectives running away
		if(!moves.get(0).commencedBy().isMrX()) throw new IllegalArgumentException("It's not mrX's turn!");


		//Keep this until replaceable
		//return moves.get(new Random().nextInt(moves.size()));
		return new Minimax((Board.GameState) board, 1, moves.get(0).source()).getBestMove();
	}
}
