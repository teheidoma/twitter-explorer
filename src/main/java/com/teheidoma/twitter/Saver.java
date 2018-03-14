package com.teheidoma.twitter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

public class Saver {
    private File file;
    private Properties properties;
    private boolean exists;

    public Saver() {
        this.file= new File(System.getProperty("user.home")+"/twitter.properties");
        this.properties = new Properties();
        this.exists = checkExists();
        if (exists) {
            try {
                this.properties.load(new FileInputStream(file));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void delete(){
        file.delete();
    }

    public void add(String key, String value){
        properties.put(key, value);
    }

    public String get(String key){
        return properties.getProperty(key);
    }

    public void save(){
        try {
            if (!exists) file.createNewFile();
            properties.store(new FileOutputStream(file), "");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean checkExists(){
        return file.exists() && file.canRead();
    }

    public boolean isExists() {
        return exists;
    }
}
