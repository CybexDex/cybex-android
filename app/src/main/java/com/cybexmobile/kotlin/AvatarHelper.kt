package com.cybexmobile.kotlin

import jdenticon.Jdenticon

class AvatarHelper {
    fun getAvatarSvg(hash: String, size: Int, padding: Float?): String {
        return Jdenticon.toSvg(hash, size, padding)

    }
}