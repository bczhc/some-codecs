package pers.zhc.tools.utils;

import android.view.MotionEvent;

public class GestureResolver {
    private GestureInterface gestureInterface;

    public GestureResolver(GestureInterface gestureInterface) {
        this.gestureInterface = gestureInterface;
    }

    private float getDistance(float x1, float x2, float y1, float y2) {
        return (float) Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    private float lastX = -1, lastY = -1;
    private float lastDistance = -1;
    private float firstDistance = -1;
    private float firstMidPointX;
    private float firstMidPointY;

    public void onTouch(MotionEvent event) {
        int pointerCount = event.getPointerCount();
        if (pointerCount == 2) {
            float x1 = event.getX(0);
            float y1 = event.getY(0);
            float x2 = event.getX(1);
            float y2 = event.getY(1);


            {
                float distance = getDistance(x1, x2, y1, y2);
                if (firstDistance == -1) {
                    firstDistance = distance;
                    firstMidPointX = (x1 + x2) / 2;
                    firstMidPointY = (y1 + y2) / 2;
                }
                if (lastDistance == -1) lastDistance = distance;
                float midPointX, midPointY;
                midPointX = (x1 + x2) / 2;
                midPointY = (y1 + y2) / 2;
                this.gestureInterface.onTwoPointZoom(firstMidPointX, firstMidPointY, midPointX, midPointY, firstDistance, distance, distance / firstDistance, distance / lastDistance, event);
                lastDistance = distance;
            }
            {
                float midX = (x1 + x2) / 2;
                float midY = (y1 + y2) / 2;
                if (lastX == -1 && lastY == -1) {
                    lastX = midX;
                    lastY = midY;
                } else {
                    this.gestureInterface.onTwoPointScroll(midX - lastX, midY - lastY, event);
                }
                this.lastX = midX;
                this.lastY = midY;
            }


        } else {
            lastX = -1;
            lastY = -1;
            firstDistance = -1;
            lastDistance = -1;
        }
    }

    public interface GestureInterface {
        /**
         * 两个点的移动
         *
         * @param distanceX x方向的变化（与上一次触摸的x距离）
         * @param distanceY y方向的变化（与上一次触摸的y距离）
         * @param event     事件
         */
        void onTwoPointScroll(float distanceX, float distanceY, MotionEvent event);

        /**
         * 两个点的缩放
         *
         * @param firstMidPointX 最开始中间点x
         * @param firstMidPointY 最开始中间点y
         * @param midPointX      当前中间点x
         * @param midPointY      当前中间点y
         * @param firstDistance  最开始两点之间的距离
         * @param distance       现在的距离
         * @param scale          与最开始的缩放比例
         * @param pScale         与上一次触摸的缩放比例
         * @param event          事件
         */
        void onTwoPointZoom(float firstMidPointX, float firstMidPointY, float midPointX, float midPointY,
                            float firstDistance, float distance, float scale, float pScale, MotionEvent event);
    }
}