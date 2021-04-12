package net.codejava.Resolve.PhaseCalc;

import net.codejava.Resolve.Model.Phase;

import java.util.ArrayList;
import java.util.concurrent.Callable;


/**
 * Класс служит расчета фазы
 *
 * @version 1.0
 */
public class PhaseCalculation extends PhaseCalculationAbstract implements Callable<Phase> {


    public PhaseCalculation(double[] temp,double leftLimit, double rightLimit) {

        this.temp = temp;
        this.leftLimit = leftLimit;
        this.rightLimit = rightLimit;
        this.N = temp.length;
    }
    @Override
    public Phase call() {
        LoadFunction();
        FFTCalculation();
        Filtration(leftLimit, rightLimit);
        IFFTCalculation();
        PhaseCalculation();
        PhaseLinking();
        if(isPhaseUnbroken()){
            NormalizingToZero();
            RemoveLinear();
            return saveFile();
        }
        else{
            return phaseToZero();
        }
    }
    private Phase phaseToZero(){
        finals = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            finals.add(0.0);
        }
        return new Phase(finals);
    }


}
