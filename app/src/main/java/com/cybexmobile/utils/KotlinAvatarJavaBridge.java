package com.cybexmobile.utils;

import com.cybexmobile.kotlin.AvatarHelper;

public class KotlinAvatarJavaBridge {

    public static String getAvatarSvg(String hash, int size, float padding) {
        return new AvatarHelper().getAvatarSvg(hash, size, padding);
    }
}
