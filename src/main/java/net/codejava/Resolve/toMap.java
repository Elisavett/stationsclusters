package net.codejava.Resolve;

import net.codejava.Resolve.Model.ResolveForm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class toMap {
    public static ArrayList<String> getGroups() throws InterruptedException, ExecutionException, IOException {
        Merger1 merger = new Merger1(ResolveForm.TempData.length, ResolveForm.arrayGroup, ResolveForm.minGroupSize);
        ArrayList<String> groupAndCoordinates = merger.run();
        return groupAndCoordinates;
    }
}