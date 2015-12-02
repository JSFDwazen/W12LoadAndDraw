/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LoadAndDraw;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.concurrent.Task;
import CalculateAndWrite.Edge;
//import jsf31kochfractalfx.JSF31KochFractalFX;

/**
 *
 * @author Stan W
 */
public class KochManager {//implements Observer {

    private int count;
    private JSF31KochFractalFX application;


    private List<Edge> edges;
    private TimeStamp tsCalc;
    private TimeStamp tsDraw;

    private Thread leftThread;
    private Thread bottomThread;
    private Thread rightThread;
    private Task leftTask;
    private Task bottomTask;
    private Task rightTask;

    private final Runtime run;

    public KochManager(JSF31KochFractalFX application) {
        this.edges = new ArrayList<>();
        this.application = application;

        tsCalc = new TimeStamp();
        tsDraw = new TimeStamp();
        count = 0;
        run = Runtime.getRuntime();
    }

    public void drawEdges(ArrayList<Edge> edges) {
        application.setTextCalc("");
        application.setTextDraw("");
        application.setTextNrEdges("");

        tsDraw.init();
        tsDraw.setBegin("start drawing");
        application.clearKochPanel();
        for (Edge e : edges) {
            Platform.runLater(()->application.drawEdge(e));        
        }

        tsDraw.setEnd("end drawing");

        application.setTextNrEdges(String.valueOf(edges.size()));
        application.setTextCalc(tsCalc.toString());
        application.setTextDraw(tsDraw.toString());
        //garbage collector
        //run.gc();
    }

    public synchronized void addEdge(Edge e) {
        edges.add(e);
    }

    public synchronized void increaseCount() {
        count++;
        if (count == 3) {
            count = 0;
            application.requestDrawEdges();
        }
    }

    public synchronized void requestDrawEdge(Edge e) {
        application.requestDrawEdge(e);
    }

    public synchronized void drawEdge(Edge e) {
        application.drawEdge(e);
    }

}
