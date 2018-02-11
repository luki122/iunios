/*
 *
 * Copyright (C) 2012 gionee Inc
 *
 * Author: fangbin
 *
 * Description:
 *
 * history
 * name                              date                                      description
 *
 */
package com.gionee.mms.popup;

public class PopUpInfoLinkedList {
    private int size;
    private PopUpInfoNode headNode;
    private PopUpInfoNode endNode;
    private PopUpInfoNode currentNode;

    public int getSize() {
        return size;
    }
    public void setSize(int size) {
        this.size = size;
    }
    public PopUpInfoNode getHeadNode() {
        return headNode;
    }
    public void setHeadNode(PopUpInfoNode headNode) {
        this.headNode = headNode;
    }
    public PopUpInfoNode getEndNode() {
        return endNode;
    }
    public void setEndNode(PopUpInfoNode endNode) {
        this.endNode = endNode;
    }
    public PopUpInfoNode getCurrentNode() {
        return currentNode;
    }
    public void setCurrentNode(PopUpInfoNode currentNode) {
        this.currentNode = currentNode;
    }

    public void addNode(PopUpInfoNode node) {
        if (!containsPopUpInfoNode(node)) {
            if (null != node) {
                if (null == headNode) {
                    headNode = node;
                    endNode = node;
                    currentNode = headNode;
                    headNode.setNextNode(endNode);
                    endNode.setPreviousNode(headNode);
                } else {
                    node.setPreviousNode(endNode);
                    endNode.setNextNode(node);
                    endNode = node;
                }
                headNode.setPreviousNode(endNode);
                endNode.setNextNode(headNode);
                size++;
                node.setIndex(size);
            }
        }
    }

    public void moveToPrevious() {
        if (null != currentNode) {
            currentNode = currentNode.getPreviousNode();
        }
    }

    public void moveToNext() {
        if (null != currentNode) {
            currentNode = currentNode.getNextNode();
        }
    }

    public PopUpInfoNode getPopUpInfoNode(int index) {
        PopUpInfoNode node = null;
        if ((0 < index) && (index <= size)) {
            if (index-1 <= size-index) {
                node = headNode;
                for (int i=0;i<index-1;i++) {
                    node = node.getNextNode();
                }
            } else {
                node = endNode;
                for (int i=0;i<size-index;i++) {
                    node = node.getPreviousNode();
                }
            }
        }
        return node;
    }

    public PopUpInfoNode removePopUpInfoNode(int index) {
        if ((0 < index) && (index <= size)) {
            PopUpInfoNode node = getPopUpInfoNode(index);
            removePopUpInfoNode(node);
            return node;
        }
        return null;
    }

    public boolean removePopUpInfoNode(PopUpInfoNode node) {
        if (containsPopUpInfoNode(node)) {
            node.getPreviousNode().setNextNode(node.getNextNode());
            node.getNextNode().setPreviousNode(node.getPreviousNode());
            if (node.getIndex() == 1) {
                headNode = node.getNextNode();
                headNode.setIndex(1);
                node = headNode;
            } else if (node.getIndex() == size) {
                endNode = endNode.getPreviousNode();
            }
            if (currentNode.getIndex() == 1) {
                currentNode = headNode;
            } else if (currentNode.getIndex() == size) {
                currentNode = endNode;
            }
            while (node.getNextNode().getIndex() != 1) {
                node = node.getNextNode();
                node.setIndex(node.getIndex()-1);
            }
            size--;
            return true;
        }
        return false;
    }

    public boolean containsPopUpInfoNode(PopUpInfoNode node) {
        boolean contains = false;
        if (!isEmpty()) {
            if (null != node) {
                PopUpInfoNode tempNode = headNode;
                for (int i=0;i<size;i++) {
                    if (tempNode.getIndex() == node.getIndex() || (null != tempNode.getMsgUri() && null != node.getMsgUri() && tempNode.getMsgUri().equals(node.getMsgUri()))) {
                        contains = true;
                        break;
                    }
                    tempNode = tempNode.getNextNode();
                }
            }
        }
        return contains;
    }

    public boolean isEmpty() {
        return size <= 0;
    }

    public void clear() {
        while(!isEmpty()) {
            PopUpInfoNode tempNode = removePopUpInfoNode(1);
            if (null != tempNode) {
                tempNode = null;
            }
        }
        headNode = null;
        endNode = null;
        currentNode = null;
    }

    public void removedSameThreadIdInfoNode(int threadId) {
        PopUpInfoNode tempNode = headNode;
        while(tempNode.getIndex() != size) {
            if (tempNode.getThreadId() == threadId) {
                removePopUpInfoNode(tempNode);
            }
            if (!isEmpty()) {
                tempNode = tempNode.getNextNode();
            }
        }
        if (endNode.getThreadId() == threadId) {
            removePopUpInfoNode(endNode);
        }
    }
}
