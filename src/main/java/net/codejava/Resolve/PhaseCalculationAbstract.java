package net.codejava.Resolve;

import net.codejava.Resolve.Model.Phase;

public abstract class PhaseCalculationAbstract {
    double[] real;
    double[] imag;
    double[] phase;
    double[] finals;
    double[] temp;
    double leftLimit;
    double rightLimit;


    public void LoadFunction(){
        real = temp.clone();
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

    protected void PhaseLinking() {
        double c = 0;
        double d = phase[1] - phase[0];
        double dtest = d;
        double n = phase.length;
        for (int i = 1; i < n; i++) {
            double dd = d;
            if (i < n - 1) d = phase[i + 1] - phase[i];
            dtest+=d;
            double cc = c;
            if (dd > Math.PI) cc = c - 2 * Math.PI;
            if (-dd > Math.PI) cc = c + 2 * Math.PI;
            phase[i] = (phase[i] + cc);
            c = cc;
        }
    }
    /*public void saveChart(){
        String chartName = index + ".jpg";
        final XYSeries series1 = new XYSeries("Phase");
        for (int i = 0; i < finals.length; i++){
            series1.add(i,finals[i]);
        }
        final XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series1);
        final JFreeChart chart = ChartFactory.createXYLineChart("Chart", "X", "Y", dataset, PlotOrientation.HORIZONTAL, true, true, false);
        try {
            ChartUtilities.saveChartAsJPEG(new File(chartName), chart, 500, 300);
        } catch (IOException ex) {
           }
    }*/

    /**
     * Нормализует фазу
     */
    protected void NormalizingToZero() {
        double a = phase[0];
        for (int i = 0; i < phase.length; i++) {
            if (phase[i] < a) a = phase[i];
        }
        for (int i = 0; i < phase.length; i++) {
            phase[i] -= a;
        }
    }

    protected void RemoveLinear() {
        finals = new double[phase.length];
        double k = (phase[phase.length - 1] - phase[0]) / (phase.length - 1);
        for (int i = 1; i <= phase.length; i++) {
            finals[i - 1] = phase[i - 1] - k * (i - 1) - phase[0];
        }
    }

    protected Phase saveFile(){
        Phase phase = new Phase(finals);
        return phase;
    }

    protected boolean isPhaseUnbroken(){
        for (int i = 1; i < phase.length-1; i++) {
            if ((phase[i + 1] - phase[i])<0) return false;
        }
        return true;
    }
    protected boolean isPhaseBroken(){
        for (int i = 1; i < phase.length-1; i++) {
            if ((phase[i + 1] - phase[i])<0) return true;
        }
        return false;
    }
}
