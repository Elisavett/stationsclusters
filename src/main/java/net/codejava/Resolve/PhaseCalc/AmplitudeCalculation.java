package net.codejava.Resolve.PhaseCalc;

import net.codejava.Resolve.Model.Phase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;


/**
 * Класс служит расчета фазы
 *
 * @version 1.0
 */
public class AmplitudeCalculation extends PhaseCalculationAbstract implements Callable<Phase> {


    public AmplitudeCalculation(double[] temp, double leftLimit, double rightLimit) {

        this.temp = temp;
        this.leftLimit = leftLimit;
        this.rightLimit = rightLimit;
    }
    @Override
    public Phase call(){
        LoadFunction();
        FFTCalculation();
        Filtration(leftLimit, rightLimit);
        IFFTCalculation();
        return new Phase(amplitudeCalculation());
    }
    public List<Double> amplitudeCalculation(){
        phase = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            phase.add(Math.sqrt(imag[i]*imag[i] + real[i]*real[i]));
        }
        return phase;
    }


}
