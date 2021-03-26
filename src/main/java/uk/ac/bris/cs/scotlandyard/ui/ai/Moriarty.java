package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Random;
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
		//Can we modify this to somehow accept a tree as a parameter to save calculation time?

		//TODO: Add the moves to a tree with calculated score
		var moves = board.getAvailableMoves().asList();

		int mrXLocation = getMrXLocation(board);

		//Generate a root node
		//Most parameters are irrelevant
		ScoreNode tree = new ScoreNode(mrXLocation, null, null, 0, board);
		for (Move m:moves) {
			//Moves with a score of zero are losses
			if (moveScore(board, m) != 0)
			tree.addChild(new ScoreNode(m.source(), tree, m, moveScore(board, m), doMove(board, m)));
		}

		//Iterate through the tree to find the best outcome
		//Create dummy bestNode so the first node found is the bestNode
		ScoreNode bestNode = new ScoreNode(0, null, null, -1, null);

		PriorityQueue<ScoreNode> queue = new PriorityQueue<>();
		queue.add(tree);
		while (!queue.isEmpty()) {
			ScoreNode node = queue.poll();
			if (node.isChildFree() && (node.getScore() > bestNode.getScore())) {
				bestNode = node;
			}
			else {
				queue.addAll(node.getChildren());
			}
		}

		//bestNode is now the node with the most optimistic outcome
		//This doesn't make much sense, as the best outcome is the detectives running away

		ArrayList<Integer> distances = new Dijkstra(board).getDistTo();

		//Keep this until replaceable
		return moves.get(new Random().nextInt(moves.size()));
	}

	//TODO
	//Returns MrX's current location
	public int getMrXLocation(Board board) {
		return -1;
	}

	//TODO
	//Return the move's score
	public int moveScore(Board board, Move m) {
		return 0;
	}

	//TODO
	//Return the board after a move
	public Board doMove(Board board, Move m) {
		return null;
	}
}
