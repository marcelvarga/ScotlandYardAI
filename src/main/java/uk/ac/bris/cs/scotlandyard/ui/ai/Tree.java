package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.model.Move;

import java.util.ArrayList;

public class Node<Integer> {
    private ArrayList<Node<Integer>> children;
    private Node<Integer> parent;
    private Integer location;
    private Integer score;

    //If not parent exists (initiator), parent = null
    public Node(Integer location, Node<Integer> parent) {
        this.location = location;
        this.parent = parent;
    }

    public Integer getLocation() {
        return this.location;
    }

    public ArrayList<Node<Integer>> getChildren() {
        return children;
    }

    public void addChild(Node child) {
        child.setParent(this);
        this.children.add(child);
    }

    public Integer numberOfChildren() {
        return Integer.valueOf(this.children.size());
    }

    public boolean isChildFree() {
        return this.children.isEmpty();
    }

    public void setParent(Node node) {
        this.parent = node;
    }

    public void disownParent() {
        this.parent = null;
    }

    //Experimental feature
    public Integer scoreAncestry() {
        return null;
    }
}

