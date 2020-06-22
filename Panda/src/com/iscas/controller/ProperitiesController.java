package com.iscas.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import com.iscas.launcher.QueryRandom;
import com.iscas.launcher.Start;

public class ProperitiesController {
    private static ProperitiesController controller;
    private Properties properties;
    private FileInputStream fileInputStream;

    public static ProperitiesController getProperitiesController() {
        if (ProperitiesController.controller == null) {
            controller = new ProperitiesController();
            return controller;
        } else {
            return controller;
        }
    }

    private ProperitiesController() {
        // TODO Auto-generated constructor stub
        try {
            this.properties = new Properties();
            this.fileInputStream = new FileInputStream(new File(
                    System.getProperty("user.dir") + "/"
                            + ExperimentEnv.properitiesName));
            properties.load(fileInputStream);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

    }

    public void loadProperitiesToEnvironmentSimple(QueryRandom queryRandom) {
        try {
            ExperimentEnv.inputDirectory = properties
                    .getProperty("inputDirectory");
            ExperimentEnv.outputDirectory = properties
                    .getProperty("outputDirectory");
            queryRandom.dataSetName = properties.getProperty("dataSetName");
            queryRandom.labelFileName = properties.getProperty("labelFileName");

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }


    public void loadProperitiesToEnvironment() {
        try {
            ExperimentEnv.inputDirectory = properties
                    .getProperty("inputDirectory");
            ExperimentEnv.outputDirectory = properties
                    .getProperty("outputDirectory");

            // 主函数中的参数
            Start.dataSetName = properties.getProperty("dataSetName");
            Start.labelFileName = properties.getProperty("labelFileName");
            Start.queryFileName = properties.getProperty("queryFileName");

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    public void stroreValueToKey(String key, String value) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(new File(
                    System.getProperty("user.dir") + "/"
                            + ExperimentEnv.properitiesName));
            properties.setProperty(key, value);
            properties.store(fileOutputStream, "");
            fileOutputStream.flush();
            fileOutputStream.close();

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return;
    }

    // 关闭连接
    public void close() {
        try {

            fileInputStream.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
