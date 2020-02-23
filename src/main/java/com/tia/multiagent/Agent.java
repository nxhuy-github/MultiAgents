package com.tia.multiagent;

import static com.tia.multiagent.Puzzle.size;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Agent extends Thread {

    private int index;
    private int sizeGrill;
    private Position currentPosition;
    private Position terminalPosition;
    private ArrayList<Position> visited;

    private static ConcurrentHashMap<Integer, ArrayList<Message>> table = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<Integer, Boolean> isFinished = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<Integer, Position> currentGrid;
    private static int totalStep;

    public Agent(int id, int size, Position currPos, Position terPos, Map cGrid) {
        this.index = id;
        this.currentPosition = currPos;
        this.sizeGrill = size;
        this.terminalPosition = terPos;
        table.putIfAbsent(id, new ArrayList<>());
        currentGrid = new ConcurrentHashMap<>(cGrid);
        totalStep = 0;
        visited = new ArrayList<>();
        visited.add(currPos);
        if (currentPosition.getX() == terminalPosition.getX()
                && currentPosition.getY() == currentPosition.getY()) {
            isFinished.put(id, Boolean.TRUE);
        } else {
            isFinished.put(id, Boolean.FALSE);
        }
    }
    
    /*
    The agent will stay alive until all agents finish their works so it can handle the message even if it came to the last position.
    If it cant find the nextPosition, it will sleep 5s and retry. 
    If it found newPos => move to that position
    */
    @Override
    public synchronized void run() {

        int step = 0;
        try {
            do {
                Position tmp;
                synchronized (this) {
                    boolean handled = handleMessage();
                    if(handled){
                        sleep(5000);
                    }
                    if (!currentPosition.equals(terminalPosition) && !handled) {
                        // System.out.println(table.toString());
                        tmp = nextPos(currentPosition, terminalPosition);
                        // System.out.println("Should I wait?");
                        while (tmp.getX() == -1 && tmp.getY() == -1) {
                            Thread.sleep(5000);
                            tmp = nextPos(currentPosition, terminalPosition);
                        }
                        // System.out.println("Thread " + index + " notify all");
                        move(tmp);
                        visited.add(tmp);
                    }
                }
                step++;
            } while (!allFinished());
            isFinished.replace(this.index, Boolean.TRUE);
            // still handle message
            if (step > totalStep) {
                totalStep = step;
            }
        } catch (InterruptedException ex) {

        }
        /*System.out.println("Send message(" + this.index + ")");
        try {
            sendMessage("Can " + this.index + " move " + this.terminalPosition.toString());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ArrayList<String> value = table.get(this);
            System.out.println("Messages what " + this.index + " need to handle: " + value.toString());
        }*/

    }

    public int findAgent(Position p) {
        for (Object key : currentGrid.keySet()) {
            Position pos = (Position) currentGrid.get(key);
            if (pos.getX() == p.getX() && pos.getY() == p.getY()) {
                return (int) key;
            }
        }
        return -1;
    }

    /*
    Standard protocal when an agent need to move. The if(isFree) is to make sure newP is not occupied (avoid desync in threads).
    isFinished value is replace accordingly.
    */
    private synchronized void move(Position newP) throws InterruptedException {
        if (isFree(newP)) {
            this.currentPosition = newP;
            currentGrid.replace(index, this.currentPosition);
            if (currentPosition.equals(terminalPosition)) {
                isFinished.replace(index, Boolean.TRUE);
            } else {
                isFinished.replace(index, Boolean.FALSE);
            }
        }

    }
    /* Handle message will move the position depend on the position of sender and receiver.
    It wont handle the message if either the sender's or receiver's location is changed since the time this message sent.
    If the sender and receiver stand in the same row (same X), receiver will move vertical (change X) and vice versa.
    */
    public synchronized boolean handleMessage() throws InterruptedException {
        
        synchronized (this) {
            // System.out.println(this.index + " handle Message...");
            ArrayList<Message> arrMessage = table.get(this.index);
            for (Message mess : arrMessage) {
                if (mess.getType() == Type.REQUEST && currentGrid.get(mess.getFrom()).equals(mess.getFromPosition()) && this.currentPosition.equals(mess.getToPosition()) ) {
                    int from = mess.getFrom();
                    int to = mess.getTo();
                    Position posTo = this.currentPosition;
                    Position posFrom = mess.getFromPosition();
                    if (posFrom.getX() == posTo.getX()) {
                        int move1 = posTo.getX() - 1 >= 0 ? posTo.getX() - 1 : -1;
                        int move2 = posTo.getX() + 1 <= 4 ? posTo.getX() + 1 : -1;
                        if (move1 != -1 && isFree(new Position(move1, posTo.getY()))) {
                            System.out.println(this.index + " from " + posTo.toString() + " will move to"
                                    + new Position(move1, posTo.getY()).toString()
                                    + "(reponse request of " + from + ")");
                            move(new Position(move1, posTo.getY()));
                            return true;
                            // this.currentPosition = new Position(move1, posFrom.getY());
                        } else {
                            if (move2 != -1 && isFree(new Position(move2, posTo.getY()))) {
                                System.out.println(this.index + " from " + posTo.toString() + " will move to"
                                        + new Position(move2, posTo.getY()).toString()
                                        + "(reponse request of " + from + ")");
                                // this.currentPosition = new Position(move2, posFrom.getY());
                                move(new Position(move2, posTo.getY()));
                                return true;
                            } else {
                                System.out.println(this.index + " from " + posTo.toString()
                                        + " can't move to" +new Position(move2, posTo.getY()).toString()
                                        + "(reponse request of " + from + ")");
                            }
                        }
                    }

                    if (posFrom.getY() == posTo.getY()) {
                        int move1 = posTo.getY() - 1 >= 0 ? posTo.getY() - 1 : -1;
                        int move2 = posTo.getY() + 1 <= 4 ? posTo.getY() + 1 : -1;
                        if (move1 != -1 && isFree(new Position(posTo.getX(), move1))) {
                            System.out.println(this.index + " from " + posTo.toString() + " will move to"
                                    + new Position(posTo.getX(), move1).toString()
                                    + "(reponse request of " + from + ")");
                            move(new Position(posTo.getX(), move1));
                            return true;
                            // this.currentPosition = new Position(posFrom.getX(), move1);
                        } else {
                            if (move2 != -1 && isFree(new Position(posTo.getX(), move2))) {
                                System.out.println(this.index + " from " + posTo.toString() + " will move to "
                                        + new Position(posTo.getX(), move2).toString()
                                        + "(reponse request of " + from + ")");
                                move(new Position(posTo.getX(), move2));
                                return true;
                                // this.currentPosition = new Position(posFrom.getX(), move2);
                            } else {
                                System.out.println(this.index + " from " + posTo.toString()
                                        + " can't move to" + new Position(posTo.getX(), move2).toString()
                                        + "(reponse request of " + from + ")");
                            }
                        }
                    }
                }
                table.replace(this.index, new ArrayList<>());
                if (mess.getType() == Type.INFORM) {

                }
            }
        }
        return false;
    }

    /*
    Inspired by http://gregtrowbridge.com/a-basic-pathfinding-algorithm/
    Test if all the positions nearby give a better/smaller distance to the final position. Continue to search even found 1 solution.
    If that position is occupied by an agent => send message demand it to move.
    */
    private Position nextPos(Position s, Position d) throws InterruptedException {
        Position nextPos = new Position(-1, -1);
        if (currentPosition.getX() > 0) {
            Position potentialPos = new Position(currentPosition);
            potentialPos.up();
            if (minStep(potentialPos, terminalPosition) <= minStep(currentPosition, terminalPosition)) {
                if (isFree(potentialPos)) {
                    nextPos = potentialPos;
                } else {
                    int to = findAgent(potentialPos);
                    //if (!isFinished.get(to)) {
                    int from = this.index;
                    Position toPos = currentGrid.get(to);
                    Message message = new Message(Type.REQUEST, from, to, this.currentPosition, toPos);
                    sendMessage(message);
                    System.out.println(this.index + " send Message to " + to);
                    //}
                }
            }
        }
        if (currentPosition.getX() < sizeGrill - 1) {
            Position potentialPos = new Position(currentPosition);
            potentialPos.down();
            if (minStep(potentialPos, terminalPosition) <= minStep(currentPosition, terminalPosition)) {
                if (isFree(potentialPos)) {
                    nextPos = potentialPos;
                } else {
                    int to = findAgent(potentialPos);
                    //if (!isFinished.get(to)) {
                    int from = this.index;
                    Position toPos = currentGrid.get(to);
                    Message message = new Message(Type.REQUEST, from, to, this.currentPosition, toPos);
                    sendMessage(message);
                    System.out.println(this.index + " send Message to " + to);

                    //}
                }
            }
        }
        if (currentPosition.getY() < sizeGrill - 1) {
            Position potentialPos = new Position(currentPosition);
            potentialPos.right();
            if (minStep(potentialPos, terminalPosition) <= minStep(currentPosition, terminalPosition)) {
                if (isFree(potentialPos)) {
                    nextPos = potentialPos;
                } else {
                    int to = findAgent(potentialPos);
                    //if (!isFinished.get(to)) {
                    int from = this.index;
                    Position toPos = currentGrid.get(to);
                    Message message = new Message(Type.REQUEST, from, to, this.currentPosition, toPos);
                    sendMessage(message);
                    System.out.println(this.index + " send Message to " + to);
                    //}
                }
            }
        }
        if (currentPosition.getY() > 0) {
            Position potentialPos = new Position(currentPosition);
            potentialPos.left();
            if (minStep(potentialPos, terminalPosition) <= minStep(currentPosition, terminalPosition)) {
                if (isFree(potentialPos)) {
                    nextPos = potentialPos;
                } else {
                    int to = findAgent(potentialPos);
                    //if (!isFinished.get(to)) {
                    int from = this.index;
                    Position toPos = currentGrid.get(to);
                    Message message = new Message(Type.REQUEST, from, to, this.currentPosition, toPos);
                    sendMessage(message);
                    System.out.println(this.index + " send Message to " + to);
                    //}
                }
            }
        }
        return nextPos;
    }

    private boolean isVisited(Position p) {
        return visited.contains(p);
    }

    private boolean allFinished() {
        for (Integer i : isFinished.keySet()) {
            if (!isFinished.get(i)) {
                return false;
            }
        }
        return true;
    }

    private int minStep(Position depart, Position dest) {
        int step = Math.abs(depart.getX() - dest.getX());
        step += Math.abs(depart.getY() - dest.getY());
        return step;
    }

    /*
    Add the message to table so the receiver can get it.
    */
    public synchronized void sendMessage(Message message) {
        synchronized (this) {
            try {
                ConcurrentHashMap.KeySetView<Integer, ArrayList<Message>> keySetView = table.keySet();
                Iterator<Integer> ite = keySetView.iterator();
                while (ite.hasNext()) {
                    int a = ite.next();
                    if (a == message.getTo()) {
                        ArrayList<Message> tmp = table.get(a);
                        tmp.add(message);
                        table.replace(a, tmp);
                        //table.get(a).add(message);
                    }
                }
                //Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public ConcurrentHashMap<Integer, ArrayList<Message>> getTable() {
        return table;
    }

    public int getIndex() {
        return index;
    }

    public int getTotalStep() {
        return totalStep;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getSizeGrill() {
        return sizeGrill;
    }

    public void setSizeGrill(int sizeGrill) {
        this.sizeGrill = sizeGrill;
    }

    public Position getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(Position currentPosition) {
        this.currentPosition = currentPosition;
    }

    public Position getTerminalPosition() {
        return terminalPosition;
    }

    public void setTerminalPosition(Position terminalPosition) {
        this.terminalPosition = terminalPosition;
    }

    public Map getCurrentGrid() {
        return currentGrid;
    }

    /*
    Check if the position is occupied by an agent or not
    */
    private synchronized boolean isFree(Position curPos) throws InterruptedException {
        sleep(500);
        synchronized (currentGrid) {
            for (Object key : currentGrid.keySet()) {
                Position pos = (Position) currentGrid.get(key);
                if (curPos.getX() == pos.getX() && curPos.getY() == pos.getY()) {
                    return false;
                }
            }
            return true;
        }
    }
}
