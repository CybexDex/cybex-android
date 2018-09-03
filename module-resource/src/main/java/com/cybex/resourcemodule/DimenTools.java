package com.cybex.resourcemodule;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.RoundingMode;
import java.text.NumberFormat;

public class DimenTools {

    public static void gen() {
        //以此文件夹下的dimens.xml文件内容为初始值参照
        File file = new File("./module-resource/src/main/res/values/dimens.xml");
        BufferedReader reader = null;

        StringBuilder w360 = new StringBuilder();
        StringBuilder w384 = new StringBuilder();
        StringBuilder w411 = new StringBuilder();
        StringBuilder w432 = new StringBuilder();
        StringBuilder w460 = new StringBuilder();
        StringBuilder w533 = new StringBuilder();
        StringBuilder w640 = new StringBuilder();

        NumberFormat format = NumberFormat.getNumberInstance();
        format.setRoundingMode(RoundingMode.DOWN);
        format.setMaximumFractionDigits(2);

        try {
            System.out.println("生成不同分辨率：");
            reader = new BufferedReader(new FileReader(file));
            String tempString;
            int line = 1;
            while ((tempString = reader.readLine()) != null) {
                if (tempString.contains("</dimen>")) {
                    String start = tempString.substring(0, tempString.indexOf(">") + 1);
                    String end = tempString.substring(tempString.lastIndexOf("<") - 2);
                    //截取<dimen></dimen>标签内的内容，从>右括号开始，到左括号减2，取得配置的数字
                    Double num = Double.parseDouble(tempString.substring(tempString.indexOf(">") + 1,
                                    tempString.indexOf("</dimen>") - 2));
                    //根据不同的尺寸，计算新的值，拼接新的字符串，并且结尾处换行
                    w360.append(start).append(format.format(num * 360 / 432)).append(end).append("\r\n");
                    w384.append(start).append(format.format(num * 384 / 432)).append(end).append("\r\n");
                    w411.append(start).append(format.format(num * 411 / 432)).append(end).append("\r\n");
                    w432.append(start).append(format.format(num * 432 / 432)).append(end).append("\r\n");
                    w460.append(start).append(format.format(num * 460 / 432)).append(end).append("\r\n");
                    w533.append(start).append(format.format(num * 533 / 432)).append(end).append("\r\n");
                    w640.append(start).append(format.format(num * 640 / 432)).append(end).append("\r\n");
                } else {
                    w360.append(tempString).append("\r\n");
                    w384.append(tempString).append("\r\n");
                    w411.append(tempString).append("\r\n");
                    w432.append(tempString).append("\r\n");
                    w460.append(tempString).append("\r\n");
                    w533.append(tempString).append("\r\n");
                    w640.append(tempString).append("\r\n");
                }
                line++;
            }
            reader.close();
            String w360file = "./module-resource/src/main/res/values-w360dp/dimens.xml";
            String w384file = "./module-resource/src/main/res/values-w384dp/dimens.xml";
            String w411file = "./module-resource/src/main/res/values-w411dp/dimens.xml";
            String w432file = "./module-resource/src/main/res/values-w432dp/dimens.xml";
            String w460file = "./module-resource/src/main/res/values-w460dp/dimens.xml";
            String w533file = "./module-resource/src/main/res/values-w533dp/dimens.xml";
            String w640file = "./module-resource/src/main/res/values-w640dp/dimens.xml";

            //将新的内容，写入到指定的文件中去
            writeFile(w360file, w360.toString());
            writeFile(w384file, w384.toString());
            writeFile(w411file, w411.toString());
            writeFile(w432file, w432.toString());
            writeFile(w460file, w460.toString());
            writeFile(w533file, w533.toString());
            writeFile(w640file, w640.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

        }

    }


    /**
     * 写入方法
     *
     */

    public static void writeFile(String file, String text) {
        PrintWriter out = null;
        try {
            out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
            out.println(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
        out.close();
    }

    public static void main(String[] args) {

        gen();

    }
}
