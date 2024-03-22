package com.hhwy.fm_baidu_map;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;

/**
 * 工具类，用于通信
 */
public class FmToolsBase {
    /**
     * 插件对象
     */
    protected final PluginRegistry.Registrar _registrar;

    // 与flutter通信
    private MethodChannel _channel;
    // flutter通道名称
    final String _name;

    /**
     * 构造函数
     * @param name 名称
     * @param registrar flutter初始类
     */
    public FmToolsBase(final Object imp, String name, PluginRegistry.Registrar registrar){
        _name = name;
        _registrar = registrar;
        _channel = new MethodChannel(_registrar.messenger(),_name);
        _channel.setMethodCallHandler(new MethodChannel.MethodCallHandler(){
            @Override
            public void onMethodCall(MethodCall methodCall, MethodChannel.Result result) {
                FmToolsBase.onMethodCall(imp,methodCall,result);
            }
        });
    }

    /**
     * 给flutter发送消息
     * @param method 方法名称
     * @param arguments 参数
     */
    public void invokeMethod(String method, Object arguments){
        if ( _channel ==null ){return;}
        _channel.invokeMethod(method, arguments);
    }

    /**
     * 销毁
     */
    public void dispose(){
        if ( _channel != null ) {
            _channel.setMethodCallHandler(null);
            _channel = null;
        }
    }

    /**
     * 通过反射调用实例方法
     * @param imp
     * @param call
     * @param result
     */
    static public void onMethodCall(Object imp, MethodCall call, MethodChannel.Result result) {
        Class<?> clazz = imp.getClass();
        try {
            if (call.arguments != null) {
                Method method = clazz.getDeclaredMethod(call.method, JSONObject.class);
                method.setAccessible(true);
                Object r = method.invoke(imp, new JSONObject((Map) call.arguments));
                result.success(r);
            } else {
                Method method = clazz.getDeclaredMethod(call.method);
                method.setAccessible(true);
                Object r = method.invoke(imp);
                result.success(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.notImplemented();
        }
    }
    /**
     * 调整图片大小
     *
     * @param bitmap
     *            源
     * @param scale_w
     *            输出宽度
     * @param scale_h
     *            输出高度
     * @return
     */
    public static Bitmap imageScale(Bitmap bitmap, float scale_w, float scale_h) {
        int src_w = bitmap.getWidth();
        int src_h = bitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(scale_w, scale_h);
        Bitmap dstbmp = Bitmap.createBitmap(bitmap, 0, 0, src_w, src_h, matrix,
                true);
        return dstbmp;
    }
    public static Bitmap textBitmap(Bitmap bitmap, String text, float textSize, int textColor) {
        return textBitmap(bitmap, text, textSize, textColor, "");
    }

    public static Bitmap textBitmap(Bitmap bitmap, String text, float textSize, int textColor, String key) {
        if (bitmap != null && !text.isEmpty()) {
            Bitmap temp = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
                    Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(temp);
            canvas.drawBitmap(bitmap, 0, 0, null);
            if (key.contains("blue_circle") || key.contains("yellow_circle") || key.contains("xiaoqu_bg")) {
                TextPaint tp = new TextPaint();
                tp.setColor(textColor);
                tp.setStyle(Style.FILL);
                if ( textSize > 0 ) {
                    tp.setTextSize(textSize);
                }
                StaticLayout myStaticLayout = new StaticLayout(text, tp, canvas.getWidth(), Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);
                // StaticLayout默认从（0，0）点开始绘制
                // 如果需要调整位置，只能在绘制之前移动Canvas的起始坐标
                if (text.contains("\n")) {
                    canvas.translate((canvas.getWidth() / 2) - (myStaticLayout.getWidth() / 2),
                            (canvas.getHeight() / 2) - ((myStaticLayout.getHeight() / 2)));
                } else {
                    canvas.translate((canvas.getWidth() / 2) - (myStaticLayout.getWidth() / 2),
                            (canvas.getHeight() / 3) - ((myStaticLayout.getHeight() / 3)));
                }
                myStaticLayout.draw(canvas);
            } else {
                Paint paint = new Paint();
                if ( textSize > 0 ) {
                    paint.setTextSize(textSize);
                }
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setColor(textColor);
                Rect rect = new Rect();
                paint.getTextBounds(text, 0, text.length(), rect);
//            float x = (bitmap.getWidth() - rect.width()) / 2f-1.0f;
                float y = (bitmap.getHeight() + rect.height()) / 2f-1.0f;
//            canvas.drawText(text, x, y, paint);
                canvas.drawText(text, bitmap.getWidth()/2.0f-1.0f, y, paint);
            }
            bitmap.recycle();
            return temp;
        }
        return bitmap;
    }

    public HashMap JsonObject2HashMap(JSONObject jo) {
        HashMap<String,Object> hm = new HashMap<>();
        for (Iterator<String> keys = jo.keys(); keys.hasNext();) {
            try {
                String key1 = keys.next();
//                if (jo.get(key1) instanceof JSONObject) {
//                    JsonObject2HashMap((JSONObject) jo.get(key1));
//                    continue;
//                }
                if(key1.equals("icon")){
                    continue;
                }
                hm.put(key1, jo.get(key1).toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return hm;
    }
}
