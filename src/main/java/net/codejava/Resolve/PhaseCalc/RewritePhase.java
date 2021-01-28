package net.codejava.Resolve.PhaseCalc;

import net.codejava.Resolve.Model.Phase;

import java.util.concurrent.Callable;


/**
 * Класс служит расчета фазы
 *
 * @version 1.0
 */
public class RewritePhase implements Callable<Phase> {

    private  double[] phase;
    public RewritePhase(double[] phase) {

        this.phase = phase;
    }
    @Override
    public Phase call() throws Exception {
            return new Phase(phase);
    }


}
