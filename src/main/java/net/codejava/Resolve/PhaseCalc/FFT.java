package net.codejava.Resolve.PhaseCalc;
/**
 * Класс служид для дискретного преобразование Фурье (DFT) и обратного дискретного преобразование Фурье (IDFT)
 *
 * @version 1.0
 */
public class FFT {

    /**
     * Вычисляет дискретное преобразование Фурье (DFT) заданного комплексного вектора,
     * сохраняя результат обратно в вектор.
     * Вектор может иметь любую длину.
     *
     * @param real Массив действительных значений
     * @param real Массив мнимых значений
     */
    public static void transform(double[] real, double[] imag) {
        if (real.length != imag.length)
            throw new IllegalArgumentException("Mismatched lengths");

        int n = real.length;
        if (n == 0)
            return;
        else if ((n & (n - 1)) == 0)  // является степенью 2
            transformRadix2(real, imag);
        else  // Более сложный алгоритм для произвольных размеров
            transformBluestein(real, imag);
    }


    /**
     * Вычисляет обратное дискретное преобразование Фурье (IDFT) заданного комплексного вектора, сохраняя результат обратно в вектор.
     * Вектор может иметь любую длину. Это преобразование не выполняет масштабирование, поэтому обратное не является истинным обратным.
     *
     * @param real Массив действительных значений
     * @param real Массив мнимых значений
     */
    public static void inverseTransform(double[] real, double[] imag) {
        transform(imag, real);
    }


    /**
     * Вычисляет дискретное преобразование Фурье (DFT) заданного комплексного вектора, сохраняя результат обратно в вектор.
     * Длина вектора должна быть степенью 2. Использует алгоритм Кули-Тьюки.
     *
     * @param real Массив действительных значений
     * @param real Массив мнимых значений
     */
    public static void transformRadix2(double[] real, double[] imag) {
        if (real.length != imag.length)
            throw new IllegalArgumentException("Mismatched lengths");
        int n = real.length;
        int levels = 31 - Integer.numberOfLeadingZeros(n);  // Equal to floor(log2(n))
        if (1 << levels != n)
            throw new IllegalArgumentException("Length is not a power of 2");
        double[] cosTable = new double[n / 2];
        double[] sinTable = new double[n / 2];
        for (int i = 0; i < n / 2; i++) {
            cosTable[i] = Math.cos(2 * Math.PI * i / n);
            sinTable[i] = Math.sin(2 * Math.PI * i / n);
        }

        // Перестановка битовых обращений
        for (int i = 0; i < n; i++) {
            int j = Integer.reverse(i) >>> (32 - levels);
            if (j > i) {
                double temp = real[i];
                real[i] = real[j];
                real[j] = temp;
                temp = imag[i];
                imag[i] = imag[j];
                imag[j] = temp;
            }
        }

        // Кули-Тьюки прореживание во времени
        for (int size = 2; size <= n; size *= 2) {
            int halfsize = size / 2;
            int tablestep = n / size;
            for (int i = 0; i < n; i += size) {
                for (int j = i, k = 0; j < i + halfsize; j++, k += tablestep) {
                    double tpre = real[j + halfsize] * cosTable[k] + imag[j + halfsize] * sinTable[k];
                    double tpim = -real[j + halfsize] * sinTable[k] + imag[j + halfsize] * cosTable[k];
                    real[j + halfsize] = real[j] - tpre;
                    imag[j + halfsize] = imag[j] - tpim;
                    real[j] += tpre;
                    imag[j] += tpim;
                }
            }
            if (size == n)  // Предотвратить переполнение в 'размер * = 2'
                break;
        }
    }


    /**
     * Вычисляет дискретное преобразование Фурье (DFT) заданного комплексного вектора, сохраняя результат обратно в вектор.
     * Вектор может иметь любую длину. Для этого требуется функция свертки, которая, в свою очередь, требует функции radix-2 FFT.
     * Использует алгоритм блюстейна Z-преобразования.
     *
     * @param real Массив действительных значений
     * @param real Массив мнимых значений
     */
    public static void transformBluestein(double[] real, double[] imag) {
        // Найти длину m степени свертки m такую, что m> = n * 2 + 1
        if (real.length != imag.length)
            throw new IllegalArgumentException("Mismatched lengths");
        int n = real.length;
        if (n >= 0x20000000)
            throw new IllegalArgumentException("Array too large");
        int m = Integer.highestOneBit(n * 2 + 1) << 1;

        // Тригонометрические таблицы
        double[] cosTable = new double[n];
        double[] sinTable = new double[n];
        for (int i = 0; i < n; i++) {
            int j = (int) ((long) i * i % (n * 2));  // This is more accurate than j = i * i
            cosTable[i] = Math.cos(Math.PI * j / n);
            sinTable[i] = Math.sin(Math.PI * j / n);
        }

        // Временные векторы и первичная обработка
        double[] areal = new double[m];
        double[] aimag = new double[m];
        for (int i = 0; i < n; i++) {
            areal[i] = real[i] * cosTable[i] + imag[i] * sinTable[i];
            aimag[i] = -real[i] * sinTable[i] + imag[i] * cosTable[i];
        }
        double[] breal = new double[m];
        double[] bimag = new double[m];
        breal[0] = cosTable[0];
        bimag[0] = sinTable[0];
        for (int i = 1; i < n; i++) {
            breal[i] = breal[m - i] = cosTable[i];
            bimag[i] = bimag[m - i] = sinTable[i];
        }

        // свертка
        double[] creal = new double[m];
        double[] cimag = new double[m];
        convolve(areal, aimag, breal, bimag, creal, cimag);

        // Постобработка
        for (int i = 0; i < n; i++) {
            real[i] = creal[i] * cosTable[i] + cimag[i] * sinTable[i];
            imag[i] = -creal[i] * sinTable[i] + cimag[i] * cosTable[i];
        }
    }


    /**
     * Вычисляет круговую свертку заданных реальных векторов. Длина каждого вектора должна быть одинаковой.
     */
    public static void convolve(double[] x, double[] y, double[] out) {
        if (x.length != y.length || x.length != out.length)
            throw new IllegalArgumentException("Mismatched lengths");
        int n = x.length;
        convolve(x, new double[n], y, new double[n], out, new double[n]);
    }


    /**
     * Вычисляет круговую свертку заданных комплексных векторов. Длина каждого вектора должна быть одинаковой.
     */
    public static void convolve(double[] xreal, double[] ximag, double[] yreal, double[] yimag, double[] outreal, double[] outimag) {
        if (xreal.length != ximag.length || xreal.length != yreal.length || yreal.length != yimag.length || xreal.length != outreal.length || outreal.length != outimag.length)
            throw new IllegalArgumentException("Mismatched lengths");

        int n = xreal.length;
        xreal = xreal.clone();
        ximag = ximag.clone();
        yreal = yreal.clone();
        yimag = yimag.clone();

        transform(xreal, ximag);
        transform(yreal, yimag);
        for (int i = 0; i < n; i++) {
            double temp = xreal[i] * yreal[i] - ximag[i] * yimag[i];
            ximag[i] = ximag[i] * yreal[i] + xreal[i] * yimag[i];
            xreal[i] = temp;
        }
        inverseTransform(xreal, ximag);
        for (int i = 0; i < n; i++) {  // Scaling (because this FFT implementation omits it)
            outreal[i] = xreal[i] / n;
            outimag[i] = ximag[i] / n;
        }
    }
}
