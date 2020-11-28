package com.duke.wechathelper.wechat

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.duke.wechathelper.Constant
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
            //窗口状态改变
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                when (className) {
                    //微信主界面
                    "com.tencent.mm.ui.LauncherUI" ->
                        if (Hawk.contains(Constant.WX_PHONE)) {
                            goToAddFriends()
                        }
                    //微信搜索微信号界面
                    "com.tencent.mm.plugin.fts.ui.FTSAddFriendUI" -> addFriends()
                }
            }
//            //获取焦点
//            AccessibilityEvent.TYPE_VIEW_FOCUSED -> {
//                when (className) {
//                    "android.widget.ListView" ->
//                        getChatList()
//                }
//            }
        }
    }

    //获取聊天记录  只能用于微信6.5.1
    private fun getChatList() {
        val nodeInfo = rootInActiveWindow

        if (nodeInfo != null) {

            val listNodes = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/a4l")
//                while ( listNodes[0].isScrollable)
//                {
//                    listNodes[0].performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)
//                }
            if (!listNodes.isEmpty()) {
                for (index in 0 until listNodes[0].childCount) {

                    val listItem = listNodes[0].getChild(index)

                    Log.d(TAG, "getChatList: "+listItem.className)
                    if (listItem.className.equals("android.widget.LinearLayout")) {
                        //日期
                        val timeNodes = listItem.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/u")
                        if (!timeNodes.isEmpty()) {
                            Log.d(TAG, "getChatList: " + "--------" + timeNodes[0].text.toString())
                        }
                        val lastTextNodes = listItem.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ij")
                        if (!lastTextNodes.isEmpty()) {
                            Log.d(TAG, "getChatList: " + "状态：-----" + lastTextNodes[0].text.toString())
                        }
                    } else if (listItem.className.equals("android.widget.RelativeLayout")) {

                        val LlNodes = listItem.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/q")
                        if (LlNodes.isEmpty()) {
                            return
                        }

                        val touxiangNodes = LlNodes[0].findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ih")
                        //日期
                        val timeNodes = LlNodes[0].parent.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/u")
                        if (!timeNodes.isEmpty()) {
                            Log.d(TAG, "getChatList: " + "--------" + timeNodes[0].text.toString())
                        }
                        //文本
                        val TxtNodes = LlNodes[0].findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ij")

                        if (!TxtNodes.isEmpty()) {
                            Log.d(TAG, "getChatList: " + touxiangNodes[0].contentDescription.toString().replace("头像", "") + "-----" + TxtNodes[0].text.toString())
                        }
                        //视频语音
                        val shipingNodes = LlNodes[0].findAccessibilityNodeInfosByViewId("com.tencent.mm:id/a_6")

                        if (!shipingNodes.isEmpty()) {
                            Log.d(TAG, "getChatList: " + touxiangNodes[0].contentDescription.toString().replace("头像", "") + "-----" + shipingNodes[0].text.toString())
                        }

                        //红包
                        val redbaketNodes = LlNodes[0].findAccessibilityNodeInfosByViewId("com.tencent.mm:id/a8g")
                        if (!redbaketNodes.isEmpty()) {
                            Log.d(TAG, "getChatList: " + touxiangNodes[0].contentDescription.toString().replace("头像", "") + "-----红包：" + redbaketNodes[0].text.toString())
                        }

                        //转账
                        val zhuanzhangNodes = LlNodes[0].findAccessibilityNodeInfosByViewId("com.tencent.mm:id/a8s")
                        val zhuanzhangMoneyNodes = LlNodes[0].findAccessibilityNodeInfosByViewId("com.tencent.mm:id/a8t")
                        if (zhuanzhangNodes.isNotEmpty() && zhuanzhangMoneyNodes.isNotEmpty()) {
                            Log.d(TAG, "getChatList: " + touxiangNodes[0].contentDescription.toString().replace("头像", "") + "-----转账：" + zhuanzhangNodes[0].text.toString()+"-----"+zhuanzhangMoneyNodes[0].text.toString())
                        }

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
            }, 500L)

        }
    }

    //主页  ->  添加好友
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
                }, 500L)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}