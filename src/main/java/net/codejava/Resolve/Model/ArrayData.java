package net.codejava.Resolve.Model;

import java.util.ArrayList;

public class ArrayData {
    ArrayList<Data> dataList = new ArrayList<>();

    public ArrayData() {
    }

    public void addData(Data data){
        dataList.add(data);
    }

    public Data getData(int index){
        return dataList.get(index);
    }

    public void clear(){
        dataList.clear();
    }
    public int size(){
        return dataList.size();
    }
}

