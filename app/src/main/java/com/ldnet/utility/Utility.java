package com.ldnet.utility;

import android.content.Context;
import android.graphics.*;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

public class Utility {

    // 获取屏幕的宽度
    public static int getScreenWidthforDIP(Context context) {

        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return px2dip(context, (float) dm.widthPixels);
    }

    //获取屏幕的宽度，像素
    public static int getScreenWidthforPX(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    // 获取屏幕的高度
    public static int getScreenHeightforDIP(Context context) {

        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return px2dip(context, (float) dm.heightPixels);
    }

    //获取屏幕的高度，像素
    public static int getScreenHeightforPX(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    // 转换dip到px
    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    // 转换px到dip
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     *
     * @param pxValue //     * @param fontScale
     *                （DisplayMetrics类中属性scaledDensity）
     * @return
     */
    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     *
     * @param spValue //     * @param fontScale
     *                （DisplayMetrics类中属性scaledDensity）
     * @return
     */
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    // 设置ListView的高度为所有Item高的和
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int totalHeight = 0;
        int maxHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            if (listItem.getMeasuredHeight() > maxHeight) {
                maxHeight = listItem.getMeasuredHeight();
            }

        }
        totalHeight = maxHeight * listView.getCount();
        ViewGroup.LayoutParams params = listView.getLayoutParams();

        params.height = totalHeight
                + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    // 设置Margin
    public static void setMargins(View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v
                    .getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }

    //把文字绘制成图像
    public static Bitmap getBitmapByText(String text, float dipW, float dipH, String frontColor, String backColor) {
        int width = dip2px(GSApplication.getInstance(), dipW);
        int height = dip2px(GSApplication.getInstance(), dipH);
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bmp);
        canvas.drawColor(Color.parseColor(backColor));
        Paint paint = new Paint();
        Typeface typeface = Typeface.DEFAULT;
        paint.setTypeface(typeface);
        paint.setColor(Color.parseColor(frontColor));
        paint.setTextSize(width / 7 * 3);
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);

        //计算绘制文字时的起始点坐标
        float tx = (width - getFontlength(paint, text)) / 2;
        float ty = (height - getFontHeight(paint)) / 2 + getFontLeading(paint);
        canvas.drawText(text, tx, ty, paint);
        return bmp;
    }

    /**
     * @return 返回指定笔和指定字符串的长度
     */
    public static float getFontlength(Paint paint, String str) {
        return paint.measureText(str);
    }

    /**
     * @return 返回指定笔的文字高度
     */
    public static float getFontHeight(Paint paint) {
        Paint.FontMetrics fm = paint.getFontMetrics();
        return fm.descent - fm.ascent;
    }

    /**
     * @return 返回指定笔离文字顶部的基准距离
     */
    public static float getFontLeading(Paint paint) {
        Paint.FontMetrics fm = paint.getFontMetrics();
        return fm.leading - fm.ascent;
    }

    public static byte[] Bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 90, baos);
        return baos.toByteArray();
    }


    public static String generateGUID() {
        return UUID.randomUUID().toString().replace("-","").trim();
    }
}
