package net.codejava.Resolve.PhaseCalc;

import net.codejava.Resolve.Model.Phase;

import java.util.ArrayList;
import java.util.List;

public abstract class PhaseCalculationAbstract {
    double[] real;
    double[] imag;
    List<Double> phase;
    List<Double> finals;
    double[] temp;
    double leftLimit;
    double rightLimit;
    int N;


    public void LoadFunction(){
        real = temp.clone();
        N = real.length;
    }


    public void FFTCalculation() {
        imag = new double[N];
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
        for (int i = 0; i < N; i++) {
            if (i < a || i > b) {
                real[i] = 0;
                imag[i] = 0;
            }
        }
    }

    public void PhaseCalculation() {
        phase = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            phase.add(Math.atan2(imag[i], real[i]));
        }

    }

    protected void PhaseLinking() {
        double c = 0;
        double d = phase.get(1) - phase.get(0);
        for (int i = 1; i < N; i++) {
            double dd = d;
            if (i < N - 1) d = phase.get(i+1) - phase.get(i);
            double cc = c;
            if (dd > Math.PI) cc = c - 2 * Math.PI;
            if (-dd > Math.PI) cc = c + 2 * Math.PI;
            phase.set(i, phase.get(i) + cc);
            c = cc;
        }
    }

    /**
     * Нормализует фазу
     */
    protected void NormalizingToZero() {
        double a = phase.get(0);
        for (double v : phase) {
            if (v < a) a = v;
        }
        for (int i = 0; i < N; i++) {
            phase.set(i, phase.get(i) - a);
        }
    }

    protected void RemoveLinear() {
        finals = new ArrayList<>();
        double k = (phase.get(N - 1) - phase.get(0)) / (N - 1);
        for (int i = 1; i <= N; i++) {
            finals.add(phase.get(i - 1) - k * (i - 1) - phase.get(0));
        }
    }

    protected Phase saveFile(){
        return new Phase(finals);
    }

    protected boolean isPhaseUnbroken(){
        for (int i = 1; i < N-1; i++) {
            if ((phase.get(i+1) - phase.get(i))<0) return false;
        }
        return true;
    }
}
