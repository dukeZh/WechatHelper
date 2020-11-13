package com.coderpig.wechathelper;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

public class ServiceUtils {

    private static AccessibilityNodeInfo findNodeInfoByViewId(AccessibilityNodeInfo nodeInfo, String viewId) {
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId(viewId);
        if (list == null || list.isEmpty()) {
            return null;
        }

        return list.get(0);
    }

    private static AccessibilityNodeInfo findNodeInfoByText(AccessibilityNodeInfo nodeInfo, String text) {
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText(text);
        if (list == null || list.isEmpty()) {
            return null;
        }

        return list.get(0);
    }

    public static void performClick(AccessibilityService service, String packageName, String viewId) {
        AccessibilityNodeInfo nodeInfo = service.getRootInActiveWindow();
        AccessibilityNodeInfo targetNode = findNodeInfoByViewId(nodeInfo, packageName + ":id/" + viewId);
        if (targetNode != null && targetNode.isClickable()) {
            targetNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
    }

    public static void performClick(AccessibilityService service, String text) {
        AccessibilityNodeInfo nodeInfo = service.getRootInActiveWindow();
        AccessibilityNodeInfo targetNode = findNodeInfoByText(nodeInfo, text);
        if (targetNode != null && targetNode.isClickable()) {
            targetNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
    }
}