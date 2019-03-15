package com.cybexmobile.kotlin

import jdenticon.Jdenticon

class AvatarHelper {
    fun getAvatarSvg(hash: String, size: Int, padding: Float?): String = Jdenticon.toSvg(hash, size, padding)
}