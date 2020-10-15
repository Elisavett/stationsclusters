package net.codejava.Resolve;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

/**
 * Класс служит для работы с файлами
 *
 * @version 1.0
 */
public class FileManager {
    public static void createFolder(String path){
        File folder = new File(path);
        if (!folder.exists()) {
            folder.mkdir();
        }
    }

    /**
     * удаляет папку, если папка не пустая, сначала удаляет файлы внутри, а затем папку.
     * @param path путь до удаляемой папки
     */
    public static void deleteFolder(String path){
        // удаляем файл рекурсивно
        recursiveDelete(new File(path));
    }

    public static void recursiveDelete(File file) {
        // до конца рекурсивного цикла
        if (!file.exists())
            return;

        //если это папка, то идем внутрь этой папки и вызываем рекурсивное удаление всего, что там есть
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                // рекурсивный вызов
                recursiveDelete(f);
            }
        }
        // вызываем метод delete() для удаления файлов и пустых(!) папок
        file.delete();
    }

    /**
     * Подсчитывает количество столбцов
     * @param path путь до файла
     * @return (int) количество столбцов
     * @throws IOException если файл не существует, это каталог, а не обычный файл, или по какой-либо другой причине не может быть открыт для чтения.
     */
    public static int getColumnsCount(String path) throws IOException{
        FileReader fr = new FileReader(path);
        Scanner scan = new Scanner(fr);
        int columnsCount = 0;
        for (int i = 0; i < 1; i++) {
            String[] array = scan.nextLine().split("\\s+");
            columnsCount = array.length;
        }
        return columnsCount;
    }
}
