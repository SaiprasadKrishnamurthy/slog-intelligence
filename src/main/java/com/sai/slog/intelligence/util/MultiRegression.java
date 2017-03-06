package com.sai.slog.intelligence.util;

import com.sai.slog.intelligence.model.PerfPredictionRequest;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

/**
 * Created by saipkri on 04/03/17.
 */
public class MultiRegression {

    public static double predict(final PerfPredictionRequest perfPredictionRequest) {
        double x[][] = perfPredictionRequest.getIndependentVars();
        double y[] = perfPredictionRequest.getDependentVars();

        OLSMultipleLinearRegression mr = new OLSMultipleLinearRegression();
        mr.newSampleData(y, x);
        double param[] = mr.estimateRegressionParameters();
        double betaHat = param[0];
        double sum = betaHat;
        for(int i=1; i<param.length; i++) {
            sum = sum + (param[i] * perfPredictionRequest.getInput()[i-1]);
        }
        return sum;
    }

    public static void main(String[] args) {
        double x[][] = {
                {3000, 100}, {6000, 512}, {10000, 1024},
                {10000, 2048}, {20000, 2048}, {20000, 3072},
                {30000, 1024}, {30000, 2048}, {30000, 3072},
                {45000, 1024}, {45000, 2048}, {45000, 3072},
                {45000, 4098}, {60000, 1024}, {60000, 2048},
                {60000, 3072}, {60000, 4098}
        },
                y[] = {100, 85, 70, 67,
                        65, 60, 66, 60,
                        55, 70, 65, 60,
                        55, 67, 60, 50, 40};
        java.text.DecimalFormat df = new java.text.DecimalFormat("####0.00000");
        OLSMultipleLinearRegression mr = new OLSMultipleLinearRegression();
        mr.newSampleData(y, x);
        double param[] = mr.estimateRegressionParameters();

        System.out.println("mr.calculateResidualSumOfSquares()"+mr.calculateResidualSumOfSquares());
        String param1String = (param[1] < 0 ? ("" +
                df.format(param[1])) : ("+" + df.format(param[1]))),
                param2String = (param[2] < 0 ? ("" +
                        df.format(param[2])) : ("+" + df.format(param[2])));
        System.out.println("y = " + df.format(param[0]) +
                param1String + "Rd" + param2String + "Du");

        System.out.println("\nThe data values are:");
        System.out.println("Rate\tData\tGiven\tPredicted");
        for (int i = 0; i < x.length; i++)
            System.out.println(x[i][0] + "\t" + x[i][1] + "\t" + y[i] + "\t" +
                    (param[0] + param[1] * x[i][0] + param[2] * x[i][1]));
    }


}
