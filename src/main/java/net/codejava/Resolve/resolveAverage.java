package net.codejava.Resolve;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.codejava.Resolve.Model.GroupAndCoordinates;
import net.codejava.Resolve.Model.GroupLine;

import java.util.ArrayList;

public class resolveAverage {
    private static final Gson GSON = new GsonBuilder().create();
    public ArrayList<String> resolve(Double r, Double[] temps, double[][] coords){

        int T_number = 0;
        int T_sr_index = 4;
        int  quantity_column = coords.length + 1;
        int quantity_line = temps.length;
        Double[][] ymenshaemoe;//массив, из которого буду вычитать(уменьшаемое)
        Double[][] source;//массив, из которого буду брать значения
        Double[][] vichitaemoe;//массив, который буду вычитать(вычитаеиое)
        Double[][] raznost;//разность массивов ymenshaemoe и chastnoe
        Double[] sr;//сюда записываю среднее значение
        Double[] conec_iterachii;//проверка конца итерации
        Double[] start_out = temps.clone();

        source = metod_ymenshaemoe(quantity_line, temps, T_number);// Массив, из которого берутся значения по умолчанию

        do
        {
            ymenshaemoe = metod_ymenshaemoe(quantity_line, temps, T_number);
            vichitaemoe = metod_vichitaemoe(quantity_line, temps, T_number);
            raznost = metod_raznost(quantity_line, ymenshaemoe, vichitaemoe, r, source);//Вычитаю из массива ymenshaemoe массив vichitaemoe, записываю в массив raznost
            sr = metod_srednee(quantity_line, raznost);
            conec_iterachii = metod_proverka(quantity_line, temps, sr, T_number);
            temps = sr;

            double endt = 0;
            for(double j : conec_iterachii)
            {
                endt += Math.abs(j);
            }
            if (endt == 0)
            {
                break;
            }
        }
        while (true);

        Double[][] out_print = new Double[quantity_line][quantity_column + 1];
        for (int i = 0; i < quantity_line; i++)
        {
            for (int j = 0; j < quantity_column - 1; j++)
            {
                out_print[i][j] = coords[j][i];

            }
            out_print[i][quantity_column] = sr[i];
        }

        method_sotrirovka(quantity_column + 1, quantity_line, out_print, T_sr_index);

        double[][] out_print_group = new double[quantity_line][quantity_column + 2];
        out_print_group = method_number_group(quantity_column + 1, quantity_line, out_print, T_sr_index);

        //запись в фаил


        return getJson(out_print_group);
    }
    public Double[][] metod_ymenshaemoe(int razmer, Double[] temp, int T_number)
    {
        Double[][] ymenshaemoe = new Double[razmer][razmer];
        for (int i = 0; i < razmer; i++)
        {
            int k = 0;
            for (int j = 0; j < razmer; j++)
            {
                int var = j + i;
                if (var >= razmer)
                {
                    var = k;
                    k++;
                }
                ymenshaemoe[i][j] = temp[var];
            }
        }
        return ymenshaemoe;
    }
    //------------------------------------------------------------------
    //-------------------------------------Заполняю массив vichitaemoe-----------------------------
    public Double[][] metod_vichitaemoe(int razmer, Double[] temp, int T_number)
    {
        Double[][] vichitaemoe = new Double[razmer][razmer];
        for (int i = 0; i < razmer; i++)
        {
            for (int j = 0; j < razmer; j++)
            {
                int var = j;
                vichitaemoe[i][j] = temp[var];
            }
        }
        return vichitaemoe;
    }
    public double[][] method_number_group(int razmer, int num_string, Double[][] nums, int index_Tc)
    {
        double[][] ddd = new double[num_string][razmer + 1];
        Double k = 1.0;
        for (int i = 0; i < num_string - 1; i++)
        {
            for (int j = 0; j < razmer; j++)
            {
                if(nums[i][j]!=null) {
                    ddd[i][j] = nums[i][j];
                    ddd[i + 1][j] = nums[i + 1][j];
                }
            }

            if (nums[i][index_Tc].equals(nums[i + 1][index_Tc]))
            {
                ddd[i][razmer] = k;
            }
                else
            {
                ddd[i][razmer] = k;
                k++;
            }
            ddd[i + 1][razmer] = k;
        }
        return ddd;
    }
    public Double[][] method_sotrirovka(int quantity_column, int num_string, Double[][] nums, int index_Tc)
    {
        //Сортировка
        Double[] temp = new Double[quantity_column];
        for (int i = 0; i < num_string - 1; i++)
        {
            for (int j = i + 1; j < num_string; j++)
            {
                if (nums[i][index_Tc] > nums[j][index_Tc])
                {
                    for (int r = 0; r < quantity_column; r++)
                    {
                        temp[r] = nums[i][r];
                        nums[i][r] = nums[j][r];
                        nums[j][r] = temp[r];
                    }
                }
            }
        }
        return nums;
    }
    //-------------------------------------Вычитаю из массива ymenshaemoe массив vichitaemoe, записываю в массив raznost-----------------------------
    public Double[][] metod_raznost(int razmer, Double[][] ymenshaemoe, Double[][] vichitaemoe, Double radius, Double[][] sourse)
    {
        Double[][] raznost = new Double[razmer][razmer];
        for (int i = 0; i < razmer; i++)
        {
            for (int j = 0; j < razmer; j++)
            {
                Double znachenie_raznosti = ymenshaemoe[i][j] - vichitaemoe[i][j];
                if (Math.abs((double)znachenie_raznosti) > radius)
                {
                    raznost[i][j] = null;
                }
                else
                {
                    raznost[i][j] = sourse[i][j];
                }
            }
        }
        return raznost;
    }

    //---------------------------------------среднее---------------------
    public Double[] metod_srednee(int razmer, Double[][] raznost)
    {
        Double[] sr = new Double[razmer];
        //меняю в масиве sr null на нули
        for (int i = 0; i < razmer; i++)
        {
            sr[i] = 0.0;
        }

        for (int i = 0; i < razmer; i++)
        {
            int n = 0;

            for (int j = 0; j < razmer; j++)
            {
                Double chislo = raznost[j][i];
                if (chislo != null)
                {
                    sr[i] = sr[i] + chislo;
                    n++;
                }
                else
                {
                    sr[i] = sr[i];
                }
            }
            sr[i] = sr[i] / n;
        }
        return sr;
    }
    //------------------------------------------------------------------

    //---------------------------------------Проверка---------------------
    public Double[] metod_proverka(int razmer, Double[] temp, Double[] sr, int T_number)
    {
        Double[] conec_iterachii = new Double[razmer];
        for (int i = 0; i < razmer; i++)
        {
            conec_iterachii[i] = temp[i] - sr[i];
        }
        return conec_iterachii;
    }
    //------------------------------------------------------------------

    public ArrayList<String> getJson(double mas[][]) {
        //формирую json файл
        ArrayList<String> json = new ArrayList<>();
        int numberGroup = 1;
        for (int i = 0; i<mas.length; i++) {
            String jsonData = GSON.toJson(new String[] {String.valueOf(mas[i][1]),
                    String.valueOf(mas[i][2]),
                    String.valueOf(mas[i][0]),
                    String.valueOf((int)mas[i][5]),
                    String.valueOf(false)});
            json.add(jsonData);

        }

//        System.out.println(json);
        return json;

    }
}

