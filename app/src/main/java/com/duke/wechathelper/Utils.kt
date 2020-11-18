package com.duke.wechathelper

import android.widget.Toast

/**
 * 描述：
 */
fun shortToast(msg: String) = Toast.makeText(HelperApp.instance, msg, Toast.LENGTH_SHORT).show()

fun longToast(msg: String) = Toast.makeText(HelperApp.instance, msg, Toast.LENGTH_LONG).show()