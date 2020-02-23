package com.tia.multiagent;

public class Position {

    private int x;
    private int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Position(Position pos) {
        this.x = pos.x;
        this.y = pos.y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void up() {
        x--;
    }
    public void down() {
        x++;
    }
    public void right() {
        y++;
    }
    public void left() {
        y--;
    }
    
    public boolean equals(Position a){
        if (x == a.x && y ==a.y){
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "coordination (X:" + x + ", Y:" + y + ")";
    }

}
