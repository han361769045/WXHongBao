package com.luleo.www.wxhongbao;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.PendingIntent;
import android.graphics.Rect;
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

    boolean isCanClick = true;
    boolean isCanClickMyself = true;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        final int eventType = event.getEventType();
        Log.e(TAG, "事件---->" + event);
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
        } else if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            receiveEnvelope(event);
        } else if (eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
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
        isCanClick = true;
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
            isCanClick = false;
            back();
        } else if ("com.tencent.mm.ui.LauncherUI".equals(event.getClassName())) {
            //在聊天界面,去点中红包
            clickEnvelope(event);
        } else if ("android.widget.ListView".equals(event.getClassName())) {
            //在聊天界面,去点中红包
            clickEnvelope(event);
        }
    }

    /**
     * 拆完红包后，详细的纪录界面 点击返回
     */
    private void back() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        //找返回按钮 点击返回
        List<AccessibilityNodeInfo> ba = nodeInfo.findAccessibilityNodeInfosByText("返回");
        if (!ba.isEmpty()) {
            click(ba.get(0).getParent());
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
        List<AccessibilityNodeInfo> over = nodeInfo.findAccessibilityNodeInfosByText("手慢了，红包派完了");
        if (over.isEmpty()) {
            Log.e(TAG, "打开红包");
            click(nodeInfo.getChild(3));
        } else {
            click(nodeInfo.getChild(2));
        }
    }

    /**
     * 判断是否有红包 在微信页面和微信聊天页面
     */
    private void clickEnvelope(AccessibilityEvent event) {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            Log.e(TAG, "rootWindow为空");
            return;
        }
        List<AccessibilityNodeInfo> listEnvelope = nodeInfo.findAccessibilityNodeInfosByText(ENVELOPE_TEXT_KEY);
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("领取红包");
        List<AccessibilityNodeInfo> receive = nodeInfo.findAccessibilityNodeInfosByText("你领取了");
        List<AccessibilityNodeInfo> look = nodeInfo.findAccessibilityNodeInfosByText("查看红包");

        Log.e(TAG, "-->listEnvelope:" + listEnvelope);
        Log.e(TAG, "-->list:" + list);
        Log.e(TAG, "-->receive:" + receive);
        Log.e(TAG, "-->look:" + look);

        if (!listEnvelope.isEmpty()) {
            //微信home（微信）页面
            for (AccessibilityNodeInfo n : listEnvelope) {
                Log.i(TAG, "在微信页面看到[微信红包]-->:" + n);
                n.getParent().getParent().getParent().getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                break;
            }
        } else if (!list.isEmpty()) {
            //最新的红包领起
            for (int i = list.size() - 1; i >= 0; i--) {
                AccessibilityNodeInfo accessibilityNodeInfo = list.get(i);
                if (isCanClick) {
                    click(accessibilityNodeInfo);
                } else {
                    Rect pc = new Rect();
                    Rect c = new Rect();
                    if (!receive.isEmpty()) {
                        receive.get(receive.size() - 1).getBoundsInScreen(c);
                    }
                    accessibilityNodeInfo.getBoundsInScreen(pc);
                    Log.e(TAG, "pc.top > c.top:" + pc.top + "----" + c.top);
                    Log.e(TAG, "list.get(i):" + list.get(i));
                    Log.e(TAG, "pc:" + pc);
                    if ((receive.isEmpty() || pc.top > c.top)) {
                        click(accessibilityNodeInfo);
                    }
                }
                break;
            }
        }
        if (!look.isEmpty()) {
            receiveMyselfEnvelope(nodeInfo, look, event);
        }
    }

    private void click(AccessibilityNodeInfo nodeInfo) {
        while (nodeInfo != null) {
            if (nodeInfo.isClickable()) {
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                break;
            }
            nodeInfo = nodeInfo.getParent();
        }
    }

    /**
     * 领取自己发的红包
     *
     * @param nodeInfo
     */
    private void receiveMyselfEnvelope(AccessibilityNodeInfo nodeInfo, List<AccessibilityNodeInfo> look, AccessibilityEvent event) {
        String str = event.getContentDescription() == null ? "" : event.getContentDescription().toString();
        //判断是否是群聊
        if (str.contains("(") && str.contains(")")) {
            List<AccessibilityNodeInfo> myself = nodeInfo.findAccessibilityNodeInfosByText("你领取了自己发的");
            List<AccessibilityNodeInfo> over = nodeInfo.findAccessibilityNodeInfosByText("你的红包已被领完");
            //最新的红包领起
            for (int i = look.size() - 1; i >= 0; i--) {
                AccessibilityNodeInfo accessibilityNodeInfo = look.get(i);
                Rect pc = new Rect();
                Rect c = new Rect();
                if (!myself.isEmpty()) {
                    myself.get(myself.size() - 1).getBoundsInScreen(c);
                }
                accessibilityNodeInfo.getBoundsInScreen(pc);
                if ((pc.top > c.top)) {
                    Log.i(TAG, "领取自己发的红包 在聊天页面-->领取红包:" + accessibilityNodeInfo);
                    //判断红包是否领完
                    if (over.isEmpty()) {
                        click(accessibilityNodeInfo);
                    } else {
                        over.get(over.size() - 1).getBoundsInScreen(c);
                        if (pc.top > c.top) {
                            click(accessibilityNodeInfo);
                        }
                    }
                    break;
                }
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

