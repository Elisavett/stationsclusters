package net.codejava.Resolve;

import java.util.concurrent.Callable;

public class WindowCalculation extends PhaseCalculationAbstract implements Callable<Integer> {

    double[] imagFTT;
    double[] realFTT;
    int center;
    boolean assimetric;

    public WindowCalculation(double[] temp, boolean assimetric, int limitLeft, int limitRight, int center) {

        this.temp = temp;
        this.leftLimit = limitLeft;
        this.rightLimit = limitRight;
        this.center = center;
        this.assimetric = assimetric;
    }

    @Override
    public Integer call() {
        int delta = center;
        int i = -1;
        if(assimetric) {
            delta = 1;
            i = 1;
        }
        LoadFunction();
        FFTCalculation();
        imagFTT = imag.clone();
        realFTT = real.clone();
        while (true) {
            imag = imagFTT.clone();
            real = realFTT.clone();
            Filtration(delta>center?0:(center - delta), center + delta);
            IFFTCalculation();
            phase = new double[realFTT.length];
            PhaseCalculation();
            PhaseLinking();
            if(assimetric){
                if (isPhaseBroken()) {
                    return delta - 1;
                }
            }
            else {
                if (isPhaseUnbroken()) {
                    return delta;
                }
            }
            delta+=i;
        }

    }
    /*@Override
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
    }*/

}





