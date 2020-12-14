package net.codejava.Resolve;

import net.codejava.Resolve.Model.Phase;

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
    public Phase call() throws Exception {
        LoadFunction();
        FFTCalculation();
        Filtration(leftLimit, rightLimit);
        IFFTCalculation();
        return new Phase(amplitudeCalculation());
    }
    public double[] amplitudeCalculation(){
        phase = new double[real.length];
        for (int i = 0; i < real.length; i++) {
            phase[i] = Math.sqrt(imag[i]*imag[i] + real[i]*real[i]);
        }
        return phase;
    }


}
