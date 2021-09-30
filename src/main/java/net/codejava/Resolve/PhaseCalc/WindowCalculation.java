package net.codejava.Resolve.PhaseCalc;

import java.util.ArrayList;
import java.util.concurrent.Callable;

/*
    Класс для рассчета окна
 */

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
        //Выбор окна фильтрации (автоматический режим)
        //Сжимаем окно и как только фаза перестает рваться, возвращаем полученную delta
        while (true) {
            imag = imagFTT.clone();
            real = realFTT.clone();
            //Фильтрация для симметричного окна
            if(assimetric)
                Filtration(delta>=center?1:(center - delta), center + delta);
            //Фильтрация для симметричного окна
            else
                Filtration(center - delta, center + delta);
            IFFTCalculation();
            phase = new ArrayList<>();
            PhaseCalculation();
            PhaseLinking();
            //Для асиметричного окна
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
            //Для симетричного окна
            else {
                //Если фаза не рвется
                if (isPhaseUnbroken()) {
                    return delta;
                }
            }
            delta+=i;
        }

    }

}





