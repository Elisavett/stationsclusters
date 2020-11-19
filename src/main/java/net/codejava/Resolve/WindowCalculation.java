package net.codejava.Resolve;

import java.util.concurrent.Callable;

public class WindowCalculation extends PhaseCalculationAbstract implements Callable<Integer> {

    double[] imagFTT;
    double[] realFTT;
    int center;

    public WindowCalculation(double[] temp, int limitLeft, int limitRight, int center) {

        this.temp = temp;
        this.leftLimit = limitLeft;
        this.rightLimit = limitRight;
        this.center = center;
    }

    /*@Override
    public Integer call() {
        int delta = 1;
        int lastWindow = center;
        LoadFunction();
        FFTCalculation();
        imagFTT = imag.clone();
        realFTT = real.clone();
        while (delta < center) {
            // while (center-leftLimit>1) {
            imag = imagFTT.clone();
            real = realFTT.clone();
            //Filtration(leftLimit, rightLimit);
            Filtration(center - delta, center + delta);
            IFFTCalculation();
            phase = new double[realFTT.length];
            PhaseCalculation();
            PhaseLinking();
            if (isPhaseBroken()) {
                //if (isPhaseUnbroken()) {
                //lastWindow =  (int)rightLimit-center;
                return delta - 1;
            }
            //leftLimit++;
            //rightLimit--;
            delta++;
        }

    }*/
    @Override
    public Integer call(){
        //int delta = 1;
        LoadFunction();
        FFTCalculation();
        imagFTT = imag.clone();
        realFTT = real.clone();
        while (true) {
            imag = imagFTT.clone();
            real = realFTT.clone();
            Filtration(leftLimit, rightLimit);
            //Filtration(center-delta, center+delta);
            IFFTCalculation();
            phase = new double[realFTT.length];
            PhaseCalculation();
            PhaseLinking();
            //if(isPhaseBroken()){
            if (isPhaseUnbroken()) {
                return (int)rightLimit-center;
                //return delta-1;
            }
            leftLimit++;
            rightLimit--;
            //delta++;
        }
    }

}





