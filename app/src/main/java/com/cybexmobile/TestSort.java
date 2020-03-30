package com.cybexmobile;

import com.cybex.provider.common.UnsignedShort;
import com.cybex.provider.graphene.chain.Utils;
import com.cybex.provider.utils.MyUtils;
import com.google.common.io.BaseEncoding;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Collator;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class TestSort {

//    public static byte toHH(int n) {
//
//    }

    public static void main(String[] args) throws Exception {

        System.out.println(new UnsignedShort((short) 20488161).intValue());
        System.out.println((short) 20488161);



//        String blockId = "0104572b7cfc27bacfd43f9400fbe9f5c10b255e";
//        String blockIdSub = blockId.substring(8, 16);
//        System.out.println(blockIdSub);
//        BaseEncoding encoding = BaseEncoding.base16().lowerCase();
//        byte[] byteContent = encoding.decode(blockIdSub);
//        System.out.println(byteContent);
//        int result = ((byteContent[3] & 0xff) << 24) | ((byteContent[2] & 0xff) << 16) | ((byteContent[1] & 0xff) << 8) | (byteContent[0] & 0xff);
//        String a = Integer.toHexString(result);
//        System.out.println(a);
//        System.out.println(Long.parseLong(a, 16));
//

//        String str = "        \t\r\n3";
//        System.out.println(str.matches("[\\s]*"));

//        char[] chars = new char[] {'\u0097'};
//        String str = new String(chars);
//        System.out.println(str);
//
//        byte[] bytes = str.getBytes();
//        System.out.println(Arrays.toString(bytes));

//        System.out.println((Integer)1 == (Integer)1);
//
//        System.out.println((Integer)222 == (Integer)222);

//        String str = null;
//        boolean result = str.equals("");
//        System.out.println(result);

//        Collator mCollator = Collator.getInstance(Locale.CHINESE);
//
//        List<String> strings = new ArrayList<>();
//        strings.add("CYB");
//        strings.add("CENNZ");
//        strings.add("DET");
//        strings.add("EOS");
//
//        Collections.sort(strings, new Comparator<String>() {
//            @Override
//            public int compare(String o1, String o2) {
//                return mCollator.compare(o1, o2) < 0 ? -1 : 1;
//            }
//        });
//
//        for(String str : strings){
//            System.out.println(str);
//        }
//        double num1 = 1000000;
//        double num2 = 0.1;
//
//        long s = 111111111000000000L;
//        long q = 3442342343560757000L;
//        System.out.println((double) q);
//
//        System.out.println(num1 * num2);
//        System.out.println(new BigDecimal(String.valueOf(q)).multiply(new BigDecimal(String.valueOf(s))).doubleValue());
//        System.out.println(num2 / num1);
//        System.out.println(new BigDecimal(String.valueOf(num2)).divide(new BigDecimal(String.valueOf(num1)), 10, RoundingMode.DOWN).doubleValue());
//
//
//        System.out.println(0.1f + 0.2f);

//        HashMap<Integer, Integer> map = new HashMap<>();
//        map.put(1, 11);
//        map.put(2, 22);
//        map.put(3, 33);
//        map.put(4, 44);
//        Integer value = map.remove(4);
//        System.out.println(value);

//        BigDecimal bigDecimal1 = new BigDecimal("123456789");
//        BigDecimal bigDecimal2 = new BigDecimal("100000");
//        System.out.println(bigDecimal1.divide(bigDecimal2, 16, RoundingMode.DOWN).doubleValue());


//        NumberFormat format = NumberFormat.getInstance();
//        format.setMinimumFractionDigits(8);
//        format.setMaximumFractionDigits(8);
//        format.setGroupingUsed(false);
//        format.setRoundingMode(RoundingMode.DOWN);
//        System.out.println(format.format(9.999999999999999E-5));
//        System.out.println(format.format(9.9999999999999999E-5));
//
//
//        BigDecimal bigDecimal = new BigDecimal(String.valueOf(9.999999999999999E-5));
//        BigDecimal bigDecimal1 = new BigDecimal(String.valueOf(9.9999999999999999E-5));
//        System.out.println(bigDecimal.setScale(8, RoundingMode.DOWN).doubleValue());
//        System.out.println(bigDecimal1.setScale(8, RoundingMode.DOWN).doubleValue());
//
//
//        DecimalFormat decimalFormat = new DecimalFormat("#0.00000000");
//        decimalFormat.setRoundingMode(RoundingMode.DOWN);
//        System.out.println(decimalFormat.format(9.999999999999999E-5));
//        System.out.println(decimalFormat.format(9.9999999999999999E-5));
//
//        System.out.println(0.0001 == 9.999999999999999E-5);
//        System.out.println(0.0001 == 9.9999999999999999E-5);

        //parse();

    }

//    private static void parse() throws Exception {
//        Gson gson = new Gson();
//        JsonParser parser = new JsonParser();
//
//        String content= getStringFromFile("/Users/dzm/Workspace_Github/cybex-android/data-provider/src/main/java/market1541718658.json");
//        JsonArray jsonArray = parser.parse(content).getAsJsonArray();
//
//        List<DjMarketVo> list = new ArrayList<>();
//
//        for(JsonElement obj : jsonArray){
//            DjMarketVo cse = gson.fromJson(obj , DjMarketVo.class);
//            list.add(cse);
//        }
//        System.out.println(list.size());
//    }
//
//    public static String convertStreamToString(InputStream is) throws Exception {
//        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
//        StringBuilder sb = new StringBuilder();
//        String line = null;
//        while ((line = reader.readLine()) != null) {
//            sb.append(line).append("\n");
//        }
//
//        reader.close();
//        return sb.toString();
//    }
//
//    public static String getStringFromFile (String filePath) throws Exception {
//        File fl = new File(filePath);
//        FileInputStream fin = new FileInputStream(fl);
//        String ret = convertStreamToString(fin);
//        //Make sure you close all streams.
//        fin.close();
//        return ret;
//    }
}
