package com.luleo.www.wxhongbao;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.PendingIntent;
import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.util.List;

/**
 * Created by leo on 2016/2/4.
 */
public class EnvelopeService extends AccessibilityService {

    static final String TAG = "leolu";

    /**
     * 微信的包名
     */
    static final String WECHAT_PACKAGENAME = "com.tencent.mm";
    /**
     * 红包消息的关键字
     */
    static final String ENVELOPE_TEXT_KEY = "[微信红包]";

    private boolean isWorking = false;

    private boolean flag = true;

    Handler handler = new Handler();


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        final int eventType = event.getEventType();
        Log.d(TAG, "事件---->" + event);
        //通知栏事件
        if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            List<CharSequence> texts = event.getText();
            if (!texts.isEmpty()) {
                for (CharSequence t : texts) {
                    String text = String.valueOf(t);
                    if (text.contains(ENVELOPE_TEXT_KEY)) {
                        if (!isWorking) {
                            openNotification(event);
                        }
                        break;
                    }
                }
            }
        } else if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            openEnvelope(event);
        } else if (eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            Log.e(TAG, "停留在聊天页面");
            openEnvelope(event);
        }
    }

    private void sendNotificationEvent(AccessibilityEvent event) {
        AccessibilityManager manager = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
        if (!manager.isEnabled()) {
            return;
        }
//        AccessibilityEvent event = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED);
//        event.setPackageName(WECHAT_PACKAGENAME);
//        event.setClassName(Notification.class.getName());
//        CharSequence tickerText = ENVELOPE_TEXT_KEY;
//        event.getText().add(tickerText);
        manager.sendAccessibilityEvent(event);
    }

    /**
     * 打开通知栏消息
     */
    private void openNotification(AccessibilityEvent event) {
        flag = true;
        if (event.getParcelableData() == null || !(event.getParcelableData() instanceof Notification)) {
            return;
        }
        //以下是精华，将微信的通知栏消息打开
        Notification notification = (Notification) event.getParcelableData();
        PendingIntent pendingIntent = notification.contentIntent;
        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    private void openEnvelope(AccessibilityEvent event) {
        if ("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI".equals(event.getClassName())) {
            //点中了红包，下一步就是去拆红包
            checkKey1();

        } else if ("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI".equals(event.getClassName())) {
            //拆完红包后看详细的纪录界面
            flag = false;
            isWorking = false;
            AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
            List<AccessibilityNodeInfo> ba = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/c4u");
            for (int i = 0; i < ba.size(); i++) {
                ba.get(i).performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }

        } else if ("com.tencent.mm.ui.LauncherUI".equals(event.getClassName())) {
            //在聊天界面,去点中红包
            checkKey2();
        } else if ("android.widget.ListView".equals(event.getClassName())) {
            Log.i(TAG, "-->停留在聊天页面:" + event.getClassName());
            checkKey3();
        }
    }

    private void checkKey1() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            Log.w(TAG, "rootWindow为空");
            return;
        }
        Log.e("==============", nodeInfo.getChildCount() + "");
        nodeInfo.getChild(3).performAction(AccessibilityNodeInfo.ACTION_CLICK);
    }

    private void checkKey2() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            Log.w(TAG, "rootWindow为空");
            return;
        }
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("领取红包");
        List<AccessibilityNodeInfo> look = nodeInfo.findAccessibilityNodeInfosByText("查看红包");
        if (list.isEmpty() && look.isEmpty()) {
            list = nodeInfo.findAccessibilityNodeInfosByText(ENVELOPE_TEXT_KEY);
            for (AccessibilityNodeInfo n : list) {
                Log.i(TAG, "-->微信红包:" + n);
                n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                break;
            }
        } else {
            //最新的红包领起
            for (int i = list.size() - 1; i >= 0; i--) {
                AccessibilityNodeInfo parent = list.get(i).getParent();
                Log.i(TAG, "-->领取红包:" + parent);
                if (parent != null) {
                    if (flag) {
                        parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                    break;
                }
            }
            if (!look.isEmpty()) {
                receiveMyselfEnvelope(nodeInfo, look);
            }
        }
    }

    private void checkKey3() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            Log.w(TAG, "rootWindow为空");
            return;
        }
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("领取红包");

        receiveEnvelope(nodeInfo, list);
    }

    /**
     * 领取别人发的红包 在聊天页面
     *
     * @param nodeInfo
     * @param list
     */
    private void receiveEnvelope(AccessibilityNodeInfo nodeInfo, List<AccessibilityNodeInfo> list) {
        List<AccessibilityNodeInfo> receive = nodeInfo.findAccessibilityNodeInfosByText("你领取了");
        //最新的红包领起
        for (int i = list.size() - 1; i >= 0; i--) {
            AccessibilityNodeInfo parent = list.get(i).getParent();
            Log.i(TAG, "领取别人发的红包 在聊天页面-->领取红包:" + parent);
            if (parent != null) {
                Rect pc = new Rect();
                Rect c = new Rect();
                if (!receive.isEmpty()) {
                    receive.get(receive.size() - 1).getBoundsInScreen(c);
                }
                parent.getBoundsInScreen(pc);
                if (receive.isEmpty() || pc.top > c.top) {
                    parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
                break;
            }
        }
        List<AccessibilityNodeInfo> look = nodeInfo.findAccessibilityNodeInfosByText("查看红包");
        if (!look.isEmpty()) {
            receiveMyselfEnvelope(nodeInfo, look);
        }
    }

    /**
     * 领取自己发的红包
     *
     * @param nodeInfo
     */
    private void receiveMyselfEnvelope(AccessibilityNodeInfo nodeInfo, List<AccessibilityNodeInfo> look) {
        List<AccessibilityNodeInfo> myself = nodeInfo.findAccessibilityNodeInfosByText("你领取了自己发的");
        List<AccessibilityNodeInfo> over = nodeInfo.findAccessibilityNodeInfosByText("你的红包已被领完");

        //最新的红包领起
        for (int i = look.size() - 1; i >= 0; i--) {
            AccessibilityNodeInfo parent = look.get(i).getParent();
            if (parent != null) {
                Rect pc = new Rect();
                Rect c = new Rect();
                if (!myself.isEmpty()) {
                    myself.get(myself.size() - 1).getBoundsInScreen(c);
                }
                parent.getBoundsInScreen(pc);
                if (myself.isEmpty() || pc.top > c.top) {
                    Log.i(TAG, "领取自己发的红包 在聊天页面-->领取红包:" + parent);
                    //判断红包是否领完
                    if (over.isEmpty()) {
                        parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    } else {
                        over.get(over.size() - 1).getBoundsInScreen(c);
                        if (pc.top > c.top) {
                            parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        }
                    }
                }
                break;
            }
        }
    }

    @Override
    public void onInterrupt() {
        Toast.makeText(this, "中断抢红包服务", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Toast.makeText(this, "连接抢红包服务", Toast.LENGTH_SHORT).show();
    }
}

