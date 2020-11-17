package net.codejava.Resolve;

import net.codejava.Resolve.Model.Phase;
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
    }
    @Override
    public Phase call() throws Exception {
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
        finals = new double[phase.length];
        for (int i = 0; i < phase.length; i++) {
            finals[i] = 0;
        }
        return new Phase(finals);
    }


}
