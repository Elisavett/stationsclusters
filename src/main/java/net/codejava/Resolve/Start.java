package net.codejava.Resolve;

import net.codejava.Resolve.Model.*;
import net.codejava.Resolve.Modules.ModulesCalc;

import java.util.ArrayList;
import java.util.concurrent.*;

public class Start {
    public ArrayList<String> run() throws InterruptedException, ExecutionException {

        ModulesCalc.PhaseAmplCalc();
        ModulesCalc.ClustersCalc();
        if (ResolveForm.classification) {
            ModulesCalc.ClassesCalc();
        }
        return ModulesCalc.JsonCalc();
    }
}
