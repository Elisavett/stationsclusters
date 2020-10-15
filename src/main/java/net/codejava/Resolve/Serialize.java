package net.codejava.Resolve;

import net.codejava.Resolve.Model.Data;

import java.io.*;
import java.util.ArrayList;

/**
 * Класс служит для сериализации классов
 *
 * @version 1.0
 */
public class Serialize {
    /**
     * Сериализует, наследуемых от класса Data
     *
     * @param path Путь для серриализованного файла
     * @param file Сериализуемый файл
     * @throws IOException если файл не существует, это каталог, а не обычный файл,
     * или по какой-либо другой причине не может быть открыт для чтения.
     */
    public static void serialize(String path, Data file) throws IOException {
        FileOutputStream outputStream = new FileOutputStream(path);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
        objectOutputStream.writeObject(file);
        objectOutputStream.close();
    }

    /**
     * Сериализует ArrayList<String>
     *
     * @param path      Путь для серриализованного файла
     * @param arrayList Сериализуемый ArrayList<String>
     * @throws IOException если файл не существует, это каталог, а не обычный файл, или по какой-либо другой причине не может быть открыт для чтения.
     */
    public static void serialize(String path, ArrayList<String> arrayList) throws IOException {
        FileOutputStream outputStream = new FileOutputStream(path);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
        objectOutputStream.writeObject(arrayList);
        objectOutputStream.close();
    }

    /**
     * Десериализует, наследуемых от класса Data
     *
     * @param path Путь серриализованного файла
     * @throws IOException            если файл не существует, это каталог, а не обычный файл, или по какой-либо другой причине не может быть открыт для чтения.
     * @throws ClassNotFoundException Класс сериализованного объекта не найден.
     */
    public static Data deserialize(String path) throws IOException, ClassNotFoundException {
        FileInputStream fileInputStream = new FileInputStream(path);
        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
        Data tempRead = (Data) objectInputStream.readObject();
        objectInputStream.close();
        return tempRead;
    }

    /**
     * Десериализует, ArrayList<String>
     *
     * @param path Путь серриализованного файла
     * @throws IOException            если файл не существует, это каталог, а не обычный файл, или по какой-либо другой причине не может быть открыт для чтения.
     * @throws ClassNotFoundException Класс сериализованного объекта не найден.
     */
    public static ArrayList<String> deserialize(String path, String array) throws IOException, ClassNotFoundException {
        FileInputStream fileInputStream = new FileInputStream(path);
        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
        ArrayList<String> tempRead = (ArrayList<String>) objectInputStream.readObject();
        objectInputStream.close();
        return tempRead;
    }
}