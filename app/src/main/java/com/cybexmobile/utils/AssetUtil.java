package com.cybexmobile.utils;

public class AssetUtil {

    public static String parseSymbol(String assetSymbol){
        if(assetSymbol == null){
            return null;
        }
        String[] symbolArr = assetSymbol.split("\\.");
        if(symbolArr.length == 1){
            return symbolArr[0];
        }
        if(symbolArr.length == 2){
            return symbolArr[1];
        }
        return "";
    }
}
