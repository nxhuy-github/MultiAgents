/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tia.multiagent;

/**
 *
 * @author Dell
 */

enum Type {REQUEST, INFORM}

public class Message {
    private Type type;
    private int to;
    private int from;
    private Position fromPosition;
    private Position toPosition;
    
    public Message (Type t, int from, int to, Position fromPosition, Position toPosition){
        this.type = t;
        this.to = to;
        this.from = from;
        this.fromPosition = fromPosition;
        this.toPosition = toPosition;
    }
    
    public Position getToPosition () {
        return this.toPosition;
    }
    
    public Position getFromPosition () {
        return this.fromPosition;
    }
    
    public int getFrom () {
        return this.from;
    }
    
    public int getTo () {
        return this.to;
    }
    
    public Type getType () {
        return this.type;
    }
    
    public void setType (Type t) {
        this.type = t;
    }
    
    @Override
    public String toString () {
        if (this.type == Type.REQUEST) {
            return "REQUEST from " + this.from;
        } else {
            return "INFORM from " + this.from;   
        }
    }
}
