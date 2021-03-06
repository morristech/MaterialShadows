package com.sdsmdg.harjot.materialshadows;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.sdsmdg.harjot.materialshadows.outlineprovider.CustomViewOutlineProvider;
import com.sdsmdg.harjot.materialshadows.utilities.GrahamScan;
import com.sdsmdg.harjot.materialshadows.utilities.Point2D;

import java.util.ArrayList;

public class MaterialShadowViewWrapper extends RelativeLayout {

    Bitmap bitmap;

    Point2D[] outlinePoints;
    Point2D[] hullPoints;
    ArrayList<Point2D> arrayListOutlinePoints;
    ArrayList<Point2D> arrayListHullPoints;
    GrahamScan grahamScan;

    Path path;

    ArrayList<Path> viewPaths;

    float offsetX = 0.0f;
    float offsetY = 0.0f;

    float shadowAlpha = 0.99f;

    public MaterialShadowViewWrapper(Context context) {
        super(context);
    }

    public MaterialShadowViewWrapper(Context context, AttributeSet attrs) {
        super(context, attrs);
        initXMLAttrs(context, attrs);
    }

    public MaterialShadowViewWrapper(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initXMLAttrs(context, attrs);
    }

    void initXMLAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MaterialShadowViewWrapper);
        final int N = a.getIndexCount();
        for (int i = 0; i < N; ++i) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.MaterialShadowViewWrapper_shadowAlpha) {
                shadowAlpha = a.getFloat(attr, 0.99f);
            } else if (attr == R.styleable.MaterialShadowViewWrapper_shadowOffsetX) {
                offsetX = a.getFloat(attr, 0.0f);
            } else if (attr == R.styleable.MaterialShadowViewWrapper_shadowOffsetY) {
                offsetY = a.getFloat(attr, 0.0f);
            }
        }
        a.recycle();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (viewPaths == null) {
            viewPaths = new ArrayList<>();
        }
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view instanceof MaterialShadowViewWrapper) {
                continue;
            }
            calculateAndRenderShadow(view, i);
        }
    }

    public float getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(float offsetX) {
        this.offsetX = offsetX;
        updateShadows(-1);
    }

    public float getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(float offsetY) {
        this.offsetY = offsetY;
        updateShadows(-1);
    }

    public float getShadowAlpha() {
        return shadowAlpha;
    }

    public void setShadowAlpha(float shadowAlpha) {
        this.shadowAlpha = shadowAlpha;
        updateShadows(-1);
    }

    void updateShadows(int pos) {
        if (pos == -1) {
            for (int i = 0; i < getChildCount(); i++) {
                path = new Path();
                path.set(viewPaths.get(i));
                path.offset(offsetX, offsetY);

                CustomViewOutlineProvider customViewOutlineProvider = new CustomViewOutlineProvider(path, shadowAlpha);
                getChildAt(i).setOutlineProvider(customViewOutlineProvider);
            }
        } else {
            path = new Path();
            path.set(viewPaths.get(pos));
            path.offset(offsetX, offsetY);

            CustomViewOutlineProvider customViewOutlineProvider = new CustomViewOutlineProvider(path, shadowAlpha);
            getChildAt(pos).setOutlineProvider(customViewOutlineProvider);
        }
    }

    void calculateAndRenderShadow(final View view, int pos) {
        view.buildDrawingCache();
        bitmap = view.getDrawingCache();

        path = new Path();

        arrayListOutlinePoints = getOutlinePoints(bitmap);
        outlinePoints = arrayListOutlinePoints.toArray(new Point2D[arrayListOutlinePoints.size()]);

        grahamScan = new GrahamScan(outlinePoints);

        arrayListHullPoints = new ArrayList<>();
        for (Point2D point2D : grahamScan.hull()) {
            arrayListHullPoints.add(point2D);
        }
        hullPoints = arrayListHullPoints.toArray(new Point2D[arrayListHullPoints.size()]);

        path.moveTo((float) hullPoints[0].x(), (float) hullPoints[0].y());
        for (int i = 1; i < hullPoints.length; i++) {
            path.lineTo((float) hullPoints[i].x(), (float) hullPoints[i].y());
        }

        if (viewPaths.size() - 1 < pos) {
            viewPaths.add(pos, path);
        } else {
            viewPaths.set(pos, path);
        }

        path.offset(offsetX, offsetY);

        CustomViewOutlineProvider customViewOutlineProvider = new CustomViewOutlineProvider(path, shadowAlpha);
        view.setOutlineProvider(customViewOutlineProvider);
    }

    ArrayList<Point2D> getOutlinePoints(Bitmap bitmap) {
        ArrayList<Point2D> arrayList = new ArrayList<>();

        for (int i = 0; i < bitmap.getHeight(); i++) {
            if (Color.alpha(bitmap.getPixel(0, i)) > 0) {
                arrayList.add(new Point2D(0, i));
            }

            if (Color.alpha(bitmap.getPixel(bitmap.getWidth() - 1, i)) > 0) {
                arrayList.add(new Point2D(bitmap.getWidth() - 1, i));
            }
        }

        for (int i = 0; i < bitmap.getHeight(); i++) {
            for (int j = 1; j < bitmap.getWidth() - 1; j++) {
                if (Color.alpha(bitmap.getPixel(j - 1, i)) == 0 && Color.alpha(bitmap.getPixel(j, i)) > 0) {
                    arrayList.add(new Point2D(j, i));
                }
                if (Color.alpha(bitmap.getPixel(j - 1, i)) > 0 && Color.alpha(bitmap.getPixel(j, i)) == 0) {
                    arrayList.add(new Point2D(j - 1, i));
                }
            }
        }
        return arrayList;
    }

}
