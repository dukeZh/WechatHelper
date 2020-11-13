package com.coderpig.wechathelper

import android.accessibilityservice.AccessibilityService
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.orhanobut.hawk.Hawk


/**
 * 描述：无障碍服务类
 */
class HelperService : AccessibilityService() {

    private val TAG = "HelperService"
    private val handler = Handler()

    override fun onServiceConnected() {
        Log.d(TAG, "onServiceConnected: ")
    }
    override fun onInterrupt() {}

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val eventType = event.eventType
        val classNameChr = event.className
        val className = classNameChr.toString()
        Log.d(TAG, event.toString())
        when (eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                if (Hawk.contains(Constant.WX_PHONE))
                {
                    when (className) {
                        //微信主界面
                        "com.tencent.mm.ui.LauncherUI" -> goToAddFriends()
                        //微信搜索微信号界面
                        "com.tencent.mm.plugin.fts.ui.FTSAddFriendUI" -> addFriends()
                    }
                }
            }
        }
    }

    //添加好友
    private fun addFriends() {
        val nodeInfo = rootInActiveWindow
        if (nodeInfo != null) {
            val tabNodes = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bhn")
            handler.postDelayed({
                val bundle = Bundle()
                bundle.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, Hawk.get(Constant.WX_PHONE))
                tabNodes[0].performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, bundle)
                Hawk.delete(Constant.WX_PHONE)
            },500L)

        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            disableSelf()
//        }
    }
    private fun goToAddFriends() {
        val nodeInfo = rootInActiveWindow
        if (nodeInfo != null) {
            val tabNodes = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/dmw")
            for (tabNode in tabNodes) {
                    tabNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        handler.postDelayed({
                            val newNodeInfo = rootInActiveWindow
                            if (newNodeInfo != null) {
                                val tagNodes = newNodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/gam")
                                for (tagNode in tagNodes) {
                                    if (tagNode.text.toString() == "添加朋友") {
                                        tagNode.parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                                    }
                                }
                            }
                        },500L)
            }
        }
    }

    //2.搜索群聊
    private fun searchGroup() {
        val nodeInfo = rootInActiveWindow
        if (nodeInfo != null) {
            val nodes = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/nr")
            for (info in nodes) {

            }
        }
    }
}