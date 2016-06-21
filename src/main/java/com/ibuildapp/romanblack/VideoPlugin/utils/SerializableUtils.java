package com.ibuildapp.romanblack.VideoPlugin.utils;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class SerializableUtils {

    /**
     * Saves a serializable object.
     *
     * @param objectToSave The object to save.
     * @param fileName The name of the file.
     * @param <T> The type of the object.
     */

    public static <T extends Serializable> void saveSerializable(T objectToSave, String fileName) {
        try {
            FileOutputStream fos = new FileOutputStream(new File(fileName));
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(objectToSave);
            os.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads a serializable object.
     *
     * @param fileName The filename.
     * @param <T> The object type.
     *
     * @return the serializable object.
     */

    public static<T extends Serializable> T readSerializable(String fileName) {
        T objectToReturn = null;

        try {
            FileInputStream fileInputStream = new FileInputStream(fileName);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            objectToReturn = (T) objectInputStream.readObject();

            objectInputStream.close();
            fileInputStream.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return objectToReturn;
    }
}