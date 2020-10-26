package net.codejava.Resolve;

import net.codejava.Resolve.Model.Phase;
import net.codejava.Resolve.Model.Temp;

import java.util.concurrent.Callable;


/**
 * Класс служит расчета фазы
 *
 * @version 1.0
 */
public class PhaseCalculation implements Callable<Phase> {
    double[] real;
    double[] imag;
    double[] phase;
    double[] finals;
    double[] temp;
    double leftLimit;
    double rightLimit;

    public PhaseCalculation(double[] temp,double leftLimit, double rightLimit) {

        this.temp = temp;
        this.leftLimit = leftLimit;
        this.rightLimit = rightLimit;
    }


    public Phase run(){
        LoadFunction();
        FFTCalculation();
       // Filtration(47, 67);
        Filtration(leftLimit, rightLimit);
        IFFTCalculation();
        PhaseCalculation();
        PhaseLinking();
        NormalizingToZero();
        RemoveLinear();
        return saveFile();
    }

    public void LoadFunction(){
        real = temp;
    }


    public void FFTCalculation() {
        imag = new double[real.length];
        FFT.transform(real, imag);
    }

    public void IFFTCalculation() {
        FFT.inverseTransform(real, imag);
    }

    /**
     * Фильтрует границы функции
     *
     * @param a Левая граница
     * @param b Правая граница
     */
    public void Filtration(double a, double b) {
        for (int i = 0; i < real.length; i++) {
            if (i <= a || i >= b) {
                real[i] = 0;
                imag[i] = 0;
            }
        }
    }

    public void PhaseCalculation() {
        phase = new double[real.length];
        for (int i = 0; i < real.length; i++) {
            phase[i] = Math.atan2(imag[i], real[i]);
        }
    }

    public void PhaseLinking() {
        double c = 0;
        double d = phase[1] - phase[0];
        double n = phase.length;
        for (int i = 1; i < n; i++) {
            double dd = d;
            if (i < n - 1) d = phase[i + 1] - phase[i];
            double cc = c;
            if (dd > Math.PI) cc = c - 2 * Math.PI;
            if (-dd > Math.PI) cc = c + 2 * Math.PI;
            phase[i] = (phase[i] + cc);
            c = cc;
        }
    }

    /**
     * Нормализует фазу
     */
    public void NormalizingToZero() {
        double a = phase[0];
        for (int i = 0; i < phase.length; i++) {
            if (phase[i] < a) a = phase[i];
        }
        for (int i = 0; i < phase.length; i++) {
            phase[i] -= a;
        }
    }

    public void RemoveLinear() {
        finals = new double[phase.length];
        double k = (phase[phase.length - 1] - phase[0]) / (phase.length - 1);
        for (int i = 1; i <= phase.length; i++) {
            finals[i - 1] = phase[i - 1] - k * (i - 1) - phase[0];
        }
    }

    private Phase saveFile(){
        Phase phase = new Phase(finals);
        return phase;
    }

    @Override
    public Phase call() throws Exception {
        LoadFunction();
        FFTCalculation();
        Filtration(leftLimit, rightLimit);
        IFFTCalculation();
        PhaseCalculation();
        PhaseLinking();
        NormalizingToZero();
        RemoveLinear();
        return saveFile();
    }
}
