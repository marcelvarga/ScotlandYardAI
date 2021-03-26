package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;

import java.util.ArrayList;

public class scoreNode{
    private scoreNode parent;
    private ArrayList<scoreNode> children;
    final private Move move;
    final private int location;
    final private int score;
    final private Board board;

    //If no parent exists (initiator), parent = null
    public scoreNode(int location, scoreNode parent, Move move, int score, Board board) {
        this.location = location;
        this.parent = parent;
        this.move = move;
        this.score = score;
        this.board = board;
    }

    public int getLocation() {
        return this.location;
    }

    public ArrayList<scoreNode> getChildren() {
        return children;
    }

    public void addChild(scoreNode child) {
        child.setParent(this);
        this.children.add(child);
    }

    public Integer numberOfChildren() {
        return this.children.size();
    }

    public boolean isChildFree() {
        return this.children.isEmpty();
    }

    public void abandonChildren() {
        this.children.clear();
    }

    public void setParent(scoreNode node) {
        this.parent = node;
    }

    public void disownParent() {
        this.parent = null;
    }

    public Move getMove() {
        return move;
    }

    public int getScore() {
        return score;
    }

    //Experimental feature
    public Integer scoreAncestry() {
        return null;
    }
}

