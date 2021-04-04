package net.codejava.Resolve;

import net.codejava.Resolve.Clustering.*;
import net.codejava.Resolve.Model.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

public class Start {
    public ArrayList<String> run() throws IOException, ClassNotFoundException, InterruptedException, ExecutionException {
        ModulesCalc.PhaseAmplCalc();
        ModulesCalc.ClustersCalc();
        if (ResolveForm.classification) {
            ModulesCalc.ClassesCalc();
        }
        return ModulesCalc.JsonCalc();
    }
}
