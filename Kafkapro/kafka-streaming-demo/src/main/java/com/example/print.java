package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class print {
    public static void main(String[] args) {
        try {
            // 通过类加载器获取资源文件的输入流，这里假设data.json在resources目录下
            InputStream inputStream = print.class.getClassLoader().getResourceAsStream("data.json");
            if (inputStream!= null) {
                // 将输入流包装成BufferedReader方便按行读取内容
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine())!= null) {
                    System.out.println(line);
                }
                reader.close();
            } else {
                System.out.println("找不到data.json文件");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}