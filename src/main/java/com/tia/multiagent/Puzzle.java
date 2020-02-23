/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tia.multiagent;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import static java.lang.Thread.sleep;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author admin
 */
public class Puzzle {

    static String input = "input";
    static String output = "output";
    static int size = 5;

    public static void main(String[] args) throws IOException, InterruptedException {
        
        Map gridStart = new HashMap();
        Map gridEnd = new HashMap();
        
        gridStart = readGrid(input);
        gridEnd = readGrid(output);
        
        System.out.println("START");
        printGrid(gridStart);
        System.out.println("END");
        printGrid(gridEnd);

        ArrayList<Thread> threads = new ArrayList<>();
        /* INIT THREADS */
        for (Object key : gridStart.keySet()) {
            Position pos = (Position) gridStart.get(key);
            Thread t = new Agent((int) key, size, (Position) gridStart.get(key), (Position) gridEnd.get(key), gridStart);
            threads.add(t);
        }
        /* START THREADS */
        for(Thread t: threads){
            t.start();
        }
        Agent a = (Agent) threads.get(0);
        int step =0;
        /* PRINT GRID WHILE IT HAVEN'T FINISHED */
        do{
            System.out.println("-----------------------------------------------------------------------");
            System.out.println("Step "+step);
            printGrid(a.getCurrentGrid());
            System.out.println("-----------------------------------------------------------------------");
            sleep(1000);
            step++;
        }while(isDiff(a.getCurrentGrid(),gridEnd));
        System.out.println("Step "+step);
        printGrid(gridEnd);
        step++;
        
        
        System.out.println("It took "+step+" steps to finish.");
        System.out.println(a.getTable().toString());
    }

    static public void printGrid(Map grid) {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                boolean found = false;
                for (Object key : grid.keySet()) {
                    Position pos = (Position) grid.get(key);
                    if (pos.getX() == i && pos.getY() == j) {
                        System.out.print((int) key);
                        found = true;
                    }
                }
                if (!found) {
                    System.out.print("0");
                }
                System.out.print(" ");
            }
            System.out.println("");
        }
    }
    /*Print grid. If there is no agent on that position -> print 0*/
    static public Map readGrid(String inputF) throws FileNotFoundException, IOException{
        Map grid = new HashMap();
        BufferedReader br = null;
        FileReader fr = null;
        fr = new FileReader(inputF);
        br = new BufferedReader(fr);
        String sCurrentLine;
        int row = 0;
        while ((sCurrentLine = br.readLine()) != null) {
            String[] splited = sCurrentLine.split("\\s+");
            for (int i = 0; i < splited.length; i++) {
                int value = Integer.parseInt(splited[i]);
                if (value != 0) {
                    grid.put(value, new Position(row, i));
                }
            }
            row++;
        }
        return grid;
    }
    
    static public boolean isDiff(Map currentGrid,Map gridEnd) {
        for (Object key : gridEnd.keySet()) {
            Position pos1 = (Position) gridEnd.get(key);
            Position pos2 = (Position) currentGrid.get(key);
            if (pos1.getX() != pos2.getX() || pos1.getY() != pos2.getY()) {
                return true;
            }
        }
        return false;
    }
}
