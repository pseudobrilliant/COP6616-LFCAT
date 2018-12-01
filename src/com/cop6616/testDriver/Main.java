package com.cop6616.testDriver;

import com.cop6616.lfcat.LFCAT;

import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;

import java.util.*;

/***
 * Test driver function for LFCAT implementation. Runs through scenarios that test throughpout under multiple conditions
 * ,threads, and lfcat parameters. Each test runs through 1,000,000 operations under different operation type distributions.
 */
public class Main {

    static final int NUMOPS = 1000000;

    /***
     * Function to pre-populate a LFCAT as part of a real world scenario.
     * @param treeSize Max tree size for the contention adaptation statistic (shoiuld rename this)
     * @return A populated LFCAT
     */
    static LFCAT<Integer> PrePopulateTree( int treeSize)
    {
        LFCAT<Integer> lfcat = new LFCAT<>();

        //sets the max tree size for the dapatation statistic
        LFCAT.SetTreeSize(treeSize);

        Vector<Thread> threads = new Vector<Thread>();

        //Going to insert a total of 500,000 values spread out over 5  threads
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

    /***
     * Provides the concurrent insert for the pre-populate function.
     */
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

    /***
     * Runs the individual test scenarios with the given paramters
     * @param test Name of test
     * @param numThreads number of threads to run test with
     * @param treeSize max treeSize threshold for contention statistic
     * @param range Max range query size
     * @param ratios Ratio of operations to perform.
     * @return
     * @throws Exception
     */
    static Double LFCATTest(String test, int numThreads, int treeSize, int range,  Ratio ratios) throws Exception
    {

        //Get a pre-populated tree
        LFCAT<Integer> lfcat = PrePopulateTree(treeSize);

        Vector<Thread> threads = new Vector<Thread>();

        //Have each thread do a portion of the total operations expected
        for(int i=0; i < numThreads; i++)
        {
            int threadops = NUMOPS / numThreads;

            if(i == numThreads - 1)
            {
                threadops += NUMOPS % numThreads;
            }

            //Create the test thread with the given lfcat, range, and expected ratios for operations
            LFCATTestThread r = new LFCATTestThread(threadops, lfcat, range, ratios);

            Thread t = new Thread(r);

            t.start();

            threads.add(t);
        }


        //Times total of all thread completions
        long startTime = System.nanoTime();

        //Singleton starter's pistol that all threads were waiting on before getting started.
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

    /***
     * Main funation that runs the test driver
     * @param args
     */
    public static void main(String[] args)
    {
        List<XYChart> charts = new ArrayList<XYChart>();

        //Keep track of all the timed results
        Vector<Integer> x = new Vector<Integer>();
        Vector<Double> rw = new Vector<Double>();
        Vector<Double> hcnt = new Vector<Double>();
        Vector<Double> lcnt = new Vector<Double>();
        Vector<Double> hcnf = new Vector<Double>();


        //Run all distribution scenarios with a varying number of threads and store the results
        for(int i =0; i <= 5; i ++)
        {
            try
            {
                x.add((int) Math.pow(2, i));

                Ratio realWorld = new Ratio(0.02f, 0.09f, 0.09f, 0.80f);
                rw.add( (float)NUMOPS / LFCATTest("Real World Test", (int) Math.pow(2, i), 10,100, realWorld));

                Ratio highContention = new Ratio(0.001f, 0.20f, 0.20f, 0.599f);
                hcnt.add( (float)NUMOPS / LFCATTest("High Contention Test", (int) Math.pow(2, i), 10,100,  highContention));

                Ratio lowContention = new Ratio(0.20f, 0.05f, 0.05f, 0.70f);
                lcnt.add( (float)NUMOPS / LFCATTest("Low Contention Test", (int) Math.pow(2, i), 10,100,  lowContention));

                Ratio highConflict = new Ratio(0.20f, 0.40f, 0.40f, 0.00f);
                hcnf.add( (float)NUMOPS / LFCATTest("High Conflict Test", (int) Math.pow(2, i), 10,100,  highConflict));

                System.out.println("");


            }
            catch(Exception e)
            {
                System.out.println("ERROR: Unable to run test!!!");
            }
        }

        //Multi range test that varies the maximum range size for the range query
        Vector<Integer>rngx = new Vector<Integer>();
        Vector<Double> rng = new Vector<Double>();

        Ratio rr = new Ratio(0.80f, 0.1f, 0.1f, 0.00f);

        for(int i =1; i <= 10; i ++)
        {
            try
            {
                int range = (int) Math.pow(2, i);
                rngx.add(range);
                rng.add((float) NUMOPS / LFCATTest("Multi Range Test ("+range+")", 16, 10, range, rr));
            }
            catch(Exception e)
            {
                System.out.println("ERROR: Unable to run test!!!");
            }
        }

        //Multi SIze tests that varie the maximum size of the nodes in the LFCAT
        Vector<Integer> treeSizex = new Vector<Integer>();
        treeSizex.add(1); treeSizex.add(10); treeSizex.add(100); treeSizex.add(500); treeSizex.add(1000); treeSizex.add(5000);
        Vector<Double> treeSize = new Vector<Double>();

        Ratio treer = new Ratio(0.02f, 0.09f, 0.09f, 0.80f);

        for(int i =0; i < treeSizex.size(); i ++)
        {
            try
            {
                int size = treeSizex.elementAt(i);
                treeSize.add((float) NUMOPS / LFCATTest("Multi Size Test ("+size+")", 16,  size,100, treer));
            }
            catch(Exception e)
            {
                System.out.println("ERROR: Unable to run test!!!");
            }
        }

        //Displays the result of the changing thread tests in one chart
        XYChart chart = new XYChartBuilder().xAxisTitle("Threads").yAxisTitle("Throughput (ops/us)").width(600).height(400).build();
        XYSeries series = chart.addSeries("Real World", x, rw);
        XYSeries series2 = chart.addSeries("High Contention", x, hcnt);
        XYSeries series3 = chart.addSeries("Low Contention", x, lcnt);
        XYSeries series4 = chart.addSeries("High Conflict", x, hcnf);
        chart.getStyler().setXAxisTickMarkSpacingHint(200);
        charts.add(chart);

        //Displays the result of the changing thread tests for an individual distribution
        chart = new XYChartBuilder().xAxisTitle("Threads").yAxisTitle("Throughput (ops/us)").width(600).height(400).build();
        series = chart.addSeries("Real World", x, rw);
        charts.add(chart);

        //Displays the result of the changing thread tests for an individual distribution
        chart = new XYChartBuilder().xAxisTitle("Threads").yAxisTitle("Throughput (ops/us)").width(600).height(400).build();
        series = chart.addSeries("High Contention", x, hcnt);
        charts.add(chart);

        //Displays the result of the changing thread tests for an individual distribution
        chart = new XYChartBuilder().xAxisTitle("Threads").yAxisTitle("Throughput (ops/us)").width(600).height(400).build();
        series = chart.addSeries("Low Contention", x, lcnt);
        charts.add(chart);

        //Displays the result of the changing thread tests for an individual distribution
        chart = new XYChartBuilder().xAxisTitle("Threads").yAxisTitle("Throughput (ops/us)").width(600).height(400).build();
        series = chart.addSeries("High Conflict", x, hcnf);
        charts.add(chart);

        //Displays the result of the changing range size test
        chart = new XYChartBuilder().xAxisTitle("Range Sizes").yAxisTitle("Throughput (ops/us)").width(600).height(400).build();
        series = chart.addSeries("Range Tests", rngx, rng);
        charts.add(chart);

        //Displays the result of the changing tree size test
        chart = new XYChartBuilder().xAxisTitle("Tree Size").yAxisTitle("Throughput (ops/us)").width(600).height(400).build();
        series = chart.addSeries("Tree Size Tests", treeSizex, treeSize);
        charts.add(chart);

        new SwingWrapper<XYChart>(charts).displayChartMatrix();
    }
}