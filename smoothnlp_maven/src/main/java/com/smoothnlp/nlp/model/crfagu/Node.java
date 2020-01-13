package com.smoothnlp.nlp.model.crfagu;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhifac on 2017/3/18.
 */
public class Node {
    public int x;  //一个句子中第几行， 初始化，见FeatureIndex.rebuildFetures()
    public int y;  //设置为第几个label  所有的都会设置一次
    public double alpha;
    public double beta;
    public double cost;
    public double bestCost;
    public Node prev;
    public List<Integer> fVector;
    public List<Path> lpath;
    public List<Path> rpath;
    public static double LOG2 = 0.69314718055;
    public static int MINUS_LOG_EPSILON = 50;

    public List<String> emKey;
    public String emStr; //仅支持一个
    public int emID; // 仅支持一个

    public Node() {
        lpath = new ArrayList<Path>();
        rpath = new ArrayList<Path>();
        clear();
        bestCost = 0.0;
        prev = null;
    }

    public static double logsumexp(double x, double y, boolean flg) {
        if (flg) {
            return y;
        }
        double vmin = Math.min(x, y);
        double vmax = Math.max(x, y);
        if (vmax > vmin + MINUS_LOG_EPSILON) {
            return vmax;
        } else {
            return vmax + Math.log(Math.exp(vmin - vmax) + 1.0);
        }
    }

    public void calcAlpha() {
        alpha = 0.0;
        for (Path p: lpath) {
            alpha = logsumexp(alpha, p.cost + p.lnode.alpha, p == lpath.get(0));
        }
        alpha += cost;
    }

    public void calcBeta() {
        beta = 0.0;
        for (Path p: rpath) {
            beta = logsumexp(beta, p.cost + p.rnode.beta, p == rpath.get(0));
        }
        beta += cost;
    }

    public void calcExpectation(double[] expected, double Z, int size) {
        double c = Math.exp(alpha + beta - cost - Z);
        for (int i = 0; fVector.get(i) != -1; i++) {
            int idx = fVector.get(i) + y;
            expected[idx] += c;
        }
        for (Path p: lpath) {
            p.calcExpectation(expected, Z, size);
        }
    }

    // support embedding templates
    public void calcExpectation(double[] expected, double [] expectedEmbedding, double Z, int size){
        double c = Math.exp(alpha + beta - cost -Z);
        for (int i = 0; fVector.get(i) != -1; i++) {
            int idx = fVector.get(i) + y;
            expected[idx] += c;
        }

        if(expectedEmbedding!=null && expectedEmbedding.length>0){
            int idx =  emID + y ;
            expectedEmbedding[idx] += c;
            System.out.println("expectedEmbedding");
        }

        for (Path p: lpath){
            p.calcExpectation(expected, Z, size);
        }

    }


    public void clear() {
        x = y = 0;
        alpha = beta = cost = 0;
        prev = null;
        fVector = null;
        lpath.clear();
        rpath.clear();
    }
}
