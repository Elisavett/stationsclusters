package net.codejava.Resolve;

import net.codejava.Resolve.Model.*;
import net.codejava.Resolve.Modules.ModulesCalc;

import java.util.ArrayList;
import java.util.concurrent.*;

public class Start {
    public ArrayList<String> run(boolean isFromPrev) throws InterruptedException, ExecutionException {

        ModulesCalc.PhaseAmplCalc();
        ModulesCalc.ClustersCalc(isFromPrev);
        if (ResolveForm.classification) {
            ModulesCalc.ClassesCalc();
        }
        return ModulesCalc.JsonCalc();
    }
}
