package net.codejava.Resolve.Clustering;

import net.codejava.Resolve.Model.ArrayData;
import net.codejava.Resolve.Model.Phase;
import net.codejava.Resolve.Model.ResolveForm;
import net.codejava.Resolve.Model.TypicalPhase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Класс служит для проверки конца итерации
 *
 * @version 1.0
 */
public class EndChecking {
    ArrayList<ArrayList<Double>> previousPhase = new ArrayList<>();
    ArrayList<ArrayList<Double>> nextPhase = new ArrayList<>();
    boolean end = false;
    int stationCount;
    List<Future<Phase>> arrayPhase;
    ArrayData arrayTypicalPath;
    ExecutorService executorService;

    public EndChecking(int stationCount, List<Future<Phase>> arrayPhase, ArrayData arrayTypicalPath,ExecutorService executorService) {
        this.stationCount = stationCount;
        this.arrayPhase = arrayPhase;
        this.arrayTypicalPath = arrayTypicalPath;
        this.executorService = executorService;
    }

    public List<Future<Phase>> run() throws ExecutionException, InterruptedException {
        phaseLoading();
        return savePhase();
    }

    public boolean check() {
        return checkEnd();
    }

    public void phaseLoading() throws ExecutionException, InterruptedException {
        for (int i = 0; i < stationCount; i++) {
            Phase phase = arrayPhase.get(i).get();
            double[] array;
            array = phase.getArray();
            previousPhase.add(new ArrayList<>());
            for (double v : array) {
                previousPhase.get(i).add(v);
            }
        }

        for (int i = 0; i < stationCount; i++) {
            TypicalPhase typicalPhase = (TypicalPhase) arrayTypicalPath.getData(i);
            double[] typicalArray;
            typicalArray = typicalPhase.getArray();
            nextPhase.add(new ArrayList<>());
            for (double v : typicalArray) {
                nextPhase.get(i).add(v);
            }
        }
    }

    private boolean comparison() {
        double max = Math.abs(previousPhase.get(0).get(0) - nextPhase.get(0).get(0));
        for (int i = 0; i < previousPhase.size(); i++) {
            for (int j = 0; j < previousPhase.get(0).size(); j++) {
                double temp = Math.abs(previousPhase.get(i).get(j) - nextPhase.get(i).get(j));
                if (temp > max)
                    max = temp;
            }
        }
        return !(max >= Double.parseDouble(ResolveForm.sigma));
        /*if (!previousPhase.equals(nextPhase)) return false;
        else return true;*/
    }

    private List<Future<Phase>> savePhase() throws InterruptedException {

        //создаем лист задач
        List<MyCallable> tasks = new ArrayList<>();
        for (int i = 0; i < nextPhase.size(); i++) {
            MyCallable myCallable = new MyCallable(i, nextPhase);
            tasks.add(myCallable);
        }
        return executorService.invokeAll(tasks);
    }

    public boolean checkEnd() {
        end = comparison();
        return end;
    }



    static class MyCallable implements Callable<Phase> {
        int position;
        ArrayList<ArrayList<Double>> nextPhase;

        public MyCallable(int position, ArrayList<ArrayList<Double>> nextPhase) {
            this.position = position;// i = position
            this.nextPhase = nextPhase;
        }

        @Override
        public Phase call(){
            double[] phaseAll = new double[nextPhase.get(position).size()];
            for (int j = 0; j < nextPhase.get(position).size(); j++) {
                phaseAll[j] = nextPhase.get(position).get(j);
            }
            return new Phase(phaseAll);
        }
    }
}
