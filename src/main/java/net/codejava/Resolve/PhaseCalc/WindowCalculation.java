package net.codejava.Resolve.PhaseCalc;

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
        int delta = center-1;
        int i = -1;
        int lastDelta = 1;
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
            //Filtration(delta>=center?1:(center - delta), delta==center?!assimetric?center + delta - 1: center + delta : center + delta);
            if(assimetric)
                Filtration(delta>=center?1:(center - delta), center + delta);
            else
                Filtration(center - delta, center + delta);
            IFFTCalculation();
            phase = new double[realFTT.length];
            PhaseCalculation();
            PhaseLinking();
            if(assimetric){
                if(delta<temp.length) {
                    if (isPhaseUnbroken()) {
                        lastDelta =  delta;
                    }
                }
                else{
                    return lastDelta;
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

}





