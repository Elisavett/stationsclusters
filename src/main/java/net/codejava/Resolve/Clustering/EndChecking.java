package net.codejava.Resolve.Clustering;

import net.codejava.Resolve.Model.Phase;
import net.codejava.Resolve.Model.ResolveForm;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Класс служит для проверки конца итерации
 */
public class EndChecking {
    private final List<Phase> previousPhases;
    private final List<Phase> nextPhases;
    private final ExecutorService executorService;

    //Передается текущая и следующая фаза
    public EndChecking(List<Phase> arrayPhase, List<Phase> typicalPhases, ExecutorService executorService) {
        this.previousPhases = arrayPhase;
        this.nextPhases = typicalPhases;
        this.executorService = executorService;
    }

    public List<Future<Phase>> run() throws InterruptedException {
        return savePhase();
    }

    public boolean check() {
        return checkEnd();
    }

    //Конец итерации - разница элементов фазы больше sigma
    private boolean checkEnd() {
        double max = Math.abs(previousPhases.get(0).getPhase().get(0) - nextPhases.get(0).getPhase().get(0));
        for (int i = 0; i < previousPhases.size(); i++) {
            List<Double> prevPhase = previousPhases.get(i).getPhase();
            List<Double> nextPhase = nextPhases.get(i).getPhase();
            for (int j = 0; j < prevPhase.size(); j++) {
                double temp = Math.abs(prevPhase.get(j) - nextPhase.get(j));
                if (temp > max)
                    max = temp;
            }
        }
        return !(max >= Double.parseDouble(ResolveForm.sigma));
    }

    private List<Future<Phase>> savePhase() throws InterruptedException {

        //создаем лист задач
        List<MyCallable> tasks = new ArrayList<>();
        for (int i = 0; i < nextPhases.size(); i++) {
            MyCallable myCallable = new MyCallable(i, nextPhases);
            tasks.add(myCallable);
        }
        return executorService.invokeAll(tasks);
    }

    static class MyCallable implements Callable<Phase> {
        int position;
        List<Phase> nextPhase;

        public MyCallable(int position, List<Phase> nextPhase) {
            this.position = position;// i = position
            this.nextPhase = nextPhase;
        }

        @Override
        public Phase call(){
            return new Phase(new ArrayList<>(nextPhase.get(position).getPhase()));
        }
    }
}
