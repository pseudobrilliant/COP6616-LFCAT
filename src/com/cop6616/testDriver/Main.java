package com.cop6616.testDriver;

import com.cop6616.lfcat.LFCAT;

import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;

import java.util.*;


public class Main {

    static final int NUMOPS = 1000000;

    static LFCAT<Integer> PrePopulateTree()
    {
        LFCAT<Integer> lfcat = new LFCAT<>();

        Vector<Thread> threads = new Vector<Thread>();

        int range = 100000;

        for(int i=0; i < 5; i++)
        {
            InsertThread r = new InsertThread(i * range, range, lfcat);

            Thread t = new Thread(r);

            t.start();

            threads.add(t);
        }

        for(int i=0; i < 5; i++)
        {
            try
            {
                threads.elementAt(i).join();
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        return lfcat;
    }

    private static class InsertThread implements Runnable
    {
        LFCAT<Integer> lfcat;
        int range =0;
        Random random;
        Vector<Integer> inserted;

        InsertThread(int _start, int _range, LFCAT<Integer> _lfcat)
        {
            range = _range;
            lfcat = _lfcat;
            random = new Random();
            inserted = new Vector<Integer>();

            for(int i = 0; i < range; i ++)
            {
                Integer rnd = random.nextInt(1000000);
                inserted.add(rnd);
            }
        }

        @Override
        public void run()
        {
            for(int i = 0; i < range; i ++)
            {
                lfcat.Insert(inserted.elementAt(i));
            }
        }
    }

    static Double LFCATTest(String test, int numThreads, int range,  Ratio ratios) throws Exception
    {

        LFCAT<Integer> lfcat = PrePopulateTree();

        Vector<Thread> threads = new Vector<Thread>();

        for(int i=0; i < numThreads; i++)
        {
            int threadops = NUMOPS / numThreads;

            if(i == numThreads - 1)
            {
                threadops += NUMOPS % numThreads;
            }

            LFCATTestThread r = new LFCATTestThread(threadops, lfcat, range, ratios);

            Thread t = new Thread(r);

            t.start();

            threads.add(t);
        }


        //Times total of all thread completions
        long startTime = System.nanoTime();

        LFCATTestThread.start = true;

        for(int i=0; i < numThreads; i++)
        {
            threads.elementAt(i).join();
        }

        long endTime = System.nanoTime();

        //Figure out how long all threads took
        double elapsedTime = ((double)(endTime - startTime) / (double)1000.0f);

        System.out.println(test + " - " + numThreads + " Threads : " +
                elapsedTime/(double) 1000000.0f +" seconds");
        System.out.flush();

        return elapsedTime;

    }

    public static void main(String[] args)
    {
        List<XYChart> charts = new ArrayList<XYChart>();

        Vector<Integer> x = new Vector<Integer>();
        Vector<Double> rw = new Vector<Double>();
        Vector<Double> hcnt = new Vector<Double>();
        Vector<Double> lcnt = new Vector<Double>();
        Vector<Double> hcnf = new Vector<Double>();

        for(int i =0; i <= 5; i ++)
        {
            try
            {
                x.add((int) Math.pow(2, i));

                Ratio realWorld = new Ratio(0.02f, 0.09f, 0.09f, 0.80f);
                rw.add( (float)NUMOPS / LFCATTest("Real World Test", (int) Math.pow(2, i), 100, realWorld));

                Ratio highContention = new Ratio(0.001f, 0.20f, 0.20f, 0.599f);
                hcnt.add( (float)NUMOPS / LFCATTest("High Contention Test", (int) Math.pow(2, i), 100,  highContention));

                Ratio lowContention = new Ratio(0.20f, 0.05f, 0.05f, 0.70f);
                lcnt.add( (float)NUMOPS / LFCATTest("Low Contention Test", (int) Math.pow(2, i), 100,  lowContention));

                Ratio highConflict = new Ratio(0.20f, 0.40f, 0.40f, 0.00f);
                hcnf.add( (float)NUMOPS / LFCATTest("High Conflict Test", (int) Math.pow(2, i), 100,  highConflict));

                System.out.println("");


            }
            catch(Exception e)
            {
                System.out.println("ERROR: Unable to run test!!!");
            }
        }

        Vector<Integer>rngx = new Vector<Integer>();
        Vector<Double> rng = new Vector<Double>();

        Ratio rr = new Ratio(0.80f, 0.1f, 0.1f, 0.00f);

        for(int i =1; i <= 15; i ++)
        {
            try
            {
                int range = (int) Math.pow(2, i);
                rngx.add(range);
                rng.add((float) NUMOPS / LFCATTest("Multi Range Test ("+range+")", 16, range, rr));
            }
            catch(Exception e)
            {
                System.out.println("ERROR: Unable to run test!!!");
            }
        }

        XYChart chart = new XYChartBuilder().xAxisTitle("Threads").yAxisTitle("Throughput (ops/us)").width(600).height(400).build();
        XYSeries series = chart.addSeries("Real World", x, rw);
        XYSeries series2 = chart.addSeries("High Contention", x, hcnt);
        XYSeries series3 = chart.addSeries("Low Contention", x, lcnt);
        XYSeries series4 = chart.addSeries("High Conflict", x, hcnf);
        chart.getStyler().setXAxisTickMarkSpacingHint(200);
        charts.add(chart);

        chart = new XYChartBuilder().xAxisTitle("Threads").yAxisTitle("Throughput (ops/us)").width(600).height(400).build();
        series = chart.addSeries("Real World", x, rw);
        charts.add(chart);

        chart = new XYChartBuilder().xAxisTitle("Threads").yAxisTitle("Throughput (ops/us)").width(600).height(400).build();
        series = chart.addSeries("High Contention", x, hcnt);
        charts.add(chart);

        chart = new XYChartBuilder().xAxisTitle("Threads").yAxisTitle("Throughput (ops/us)").width(600).height(400).build();
        series = chart.addSeries("Low Contention", x, lcnt);
        charts.add(chart);

        chart = new XYChartBuilder().xAxisTitle("Threads").yAxisTitle("Throughput (ops/us)").width(600).height(400).build();
        series = chart.addSeries("High Conflict", x, hcnf);
        charts.add(chart);

        chart = new XYChartBuilder().xAxisTitle("Range Sizes").yAxisTitle("Throughput (ops/us)").width(600).height(400).build();
        series = chart.addSeries("Range Tests", rngx, rng);
        charts.add(chart);


        new SwingWrapper<XYChart>(charts).displayChartMatrix();
    }
}