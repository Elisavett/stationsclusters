package net.codejava.Resolve.PhaseCalc;

import net.codejava.Resolve.Model.Phase;
import net.codejava.Resolve.Model.ResolveForm;

import java.util.ArrayList;
import java.util.concurrent.Callable;


/*
 * Класс служит расчета фазы
 */
public class PhaseCalculation extends PhaseCalculationAbstract implements Callable<Phase> {


    public PhaseCalculation(double[] temp, double leftLimit, double rightLimit) {

        this.temp = temp;
        this.leftLimit = leftLimit;
        this.rightLimit = rightLimit;
        this.N = temp.length;
    }

    //Процесс рассчета фазы
    @Override
    public Phase call() {
        LoadFunction();
        FFTCalculation();
        Filtration(leftLimit, rightLimit);
        IFFTCalculation();
        PhaseCalculation();
        PhaseLinking();

        //Проверка не рвется ли фаза
        if (isPhaseUnbroken()) {
            //Если нужно дотягивать до нуля, выполняем дотяжку
            if (ResolveForm.phaseToZero) {
                NormalizingToZero();
            }
            RemoveLinear();
            return saveFile();
        } else {
            //Если фаза рвется, то обнуляем фазу
            return phaseToZero();
        }
    }

    //Обнуление фазы
    private Phase phaseToZero(){
        finals = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            finals.add(0.0);
        }
        return new Phase(finals);
    }


}
