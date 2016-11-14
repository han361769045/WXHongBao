package com.luleo.www.wxhongbao;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.PendingIntent;
import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    Handler handler = new Handler();

    Map<AccessibilityNodeInfo, Boolean> maps = new HashMap<>();

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
                        openNotification(event);
                        break;
                    }
                }
            }
        } else if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            receiveEnvelope(event);
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

    private void receiveEnvelope(AccessibilityEvent event) {
        Log.e(TAG, "event.getClassName-->:" + event.getClassName());
        if ("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI".equals(event.getClassName())) {
            //去拆红包（点击 【开】）
            openEnvelope();
        } else if ("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI".equals(event.getClassName())) {
            //拆完红包后，详细的纪录界面
            back();
        } else if ("com.tencent.mm.ui.LauncherUI".equals(event.getClassName()) || "android.widget.ListView".equals(event.getClassName())) {
            //在聊天界面,去点中红包
            clickEnvelope();
        }
    }

    /**
     * 拆完红包后，详细的纪录界面 点击返回
     */
    private void back() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        //找返回按钮 点击返回
        List<AccessibilityNodeInfo> ba = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/gd");
        for (int i = 0; i < ba.size(); i++) {
            ba.get(i).getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
    }

    /**
     * 打开红包
     */
    private void openEnvelope() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            Log.e(TAG, "rootWindow为空");
            return;
        }
        Log.e("openEnvelope", nodeInfo.getChildCount() + "");
        Log.e("openEnvelope", nodeInfo.getChildCount() + nodeInfo.getChild(3).toString());
        Log.e("openEnvelope", nodeInfo.getChildCount() + nodeInfo.getChild(3).getViewIdResourceName());
        nodeInfo.getChild(3).performAction(AccessibilityNodeInfo.ACTION_CLICK);
    }

    /**
     * 判断是否有红包 在微信页面和微信聊天页面
     */
    private void clickEnvelope() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            Log.w(TAG, "rootWindow为空");
            return;
        }
        List<AccessibilityNodeInfo> listEnvelope = nodeInfo.findAccessibilityNodeInfosByText(ENVELOPE_TEXT_KEY);
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("领取红包");
        List<AccessibilityNodeInfo> receive = nodeInfo.findAccessibilityNodeInfosByText("你领取了");
        List<AccessibilityNodeInfo> look = nodeInfo.findAccessibilityNodeInfosByText("查看红包");

        Log.i(TAG, "-->listEnvelope:" + listEnvelope);
        Log.i(TAG, "-->list:" + list);
        Log.i(TAG, "-->receive:" + receive);
        Log.i(TAG, "-->look:" + look);

        if (listEnvelope != null && !listEnvelope.isEmpty()) {
            //微信home（微信）页面
            for (AccessibilityNodeInfo n : listEnvelope) {
                Log.i(TAG, "-->微信红包:" + n);
                n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                break;
            }
        } else if (list != null && !list.isEmpty()) {

            for (AccessibilityNodeInfo accessibilityNodeInfo : list) {
                if (!maps.containsKey(accessibilityNodeInfo)) {
                    maps.put(accessibilityNodeInfo, false);
                }
            }
            //最新的红包领起
            for (int i = list.size() - 1; i >= 0; i--) {
                AccessibilityNodeInfo parent = list.get(i).getParent();
                Log.e(TAG, "领取别人发的红包 在聊天页面-->领取红包:" + parent);
                Log.e(TAG, "领取别人发的红包 在聊天页面-->你领取了:" + receive);
                if (parent != null) {
                    Rect pc = new Rect();
                    Rect c = new Rect();
                    if (!receive.isEmpty()) {
                        receive.get(receive.size() - 1).getParent().getBoundsInScreen(c);
                    }
                    parent.getBoundsInScreen(pc);
                    Log.e(TAG, "pc.top > c.top:" + pc.top + "----" + c.top);
                    Log.e(TAG, "list.get(i):" + list.get(i));
                    Log.e(TAG, "maps.get(list.get(i)):" + maps.get(list.get(i)));
                    if ((receive.isEmpty() || pc.top > c.top) && !maps.get(list.get(i))) {
                        maps.put(list.get(i), true);
                        parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                    break;
                }
            }
        } else if (look != null && !look.isEmpty()) {
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


    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        //接收按键事件

        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_A: {
                Toast.makeText(this, "A", Toast.LENGTH_SHORT).show();
                break;
            }
            case KeyEvent.KEYCODE_B: {
                Toast.makeText(this, "B", Toast.LENGTH_SHORT).show();
                break;
            }
            case KeyEvent.KEYCODE_C: {
                Toast.makeText(this, "C", Toast.LENGTH_SHORT).show();
                break;
            }
            case KeyEvent.KEYCODE_D: {
                Toast.makeText(this, "D", Toast.LENGTH_SHORT).show();
                break;
            }
            case KeyEvent.KEYCODE_E: {
                Toast.makeText(this, "E", Toast.LENGTH_SHORT).show();
                break;
            }
            case KeyEvent.KEYCODE_G: {
                Toast.makeText(this, "G", Toast.LENGTH_SHORT).show();
                break;
            }
        }
        return super.onKeyEvent(event);
    }

}

