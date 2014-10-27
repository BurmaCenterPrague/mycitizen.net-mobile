package net.mycitizen.mcn;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.graphics.Point;
import android.graphics.Rect;


public class MapOverlay extends Overlay {

    GeoPoint myLocation = null;
    GeoPoint center = null;

    int radius = 0;

    int width, height;

    Context ctx;
    MapView mp;

    public MapOverlay(Context ctx, int width, int height, GeoPoint center, GeoPoint myLocation) {
        super(ctx);
        this.ctx = ctx;

        this.width = width;
        this.height = height;

        this.center = center;
        this.myLocation = myLocation;
    }

    public void updateMyPosition(GeoPoint c) {
        center = c;

    }

    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        Paint paint = new Paint();
        paint.setStyle(Style.FILL);
        paint.setColor(Color.RED);
        paint.setStrokeWidth((float) 4);
        paint.setAlpha(40);

        Paint paintR = new Paint();
        paintR.setStyle(Style.STROKE);
        paintR.setColor(Color.RED);
        paintR.setStrokeWidth((float) 4);


        mp = mapView;

        width = canvas.getWidth();
        height = canvas.getHeight();

        Projection p = mapView.getProjection();

        Rect screen = p.getScreenRect();

        Point point = new Point();


        if (center != null) {
            p.toPixels(center, point);


            if (radius > 0) {
                int radius_circle = (int) mapView.getProjection().metersToEquatorPixels(radius);
                canvas.drawCircle(point.x, point.y, radius_circle, paint);
                canvas.drawCircle(point.x, point.y, radius_circle, paintR);
            }

            Drawable map_point_center = ctx.getResources().getDrawable(ctx.getResources().getIdentifier("my_location", "drawable", "net.mycitizen.mcn"));

            map_point_center.setBounds(new Rect(point.x - 25, point.y - 25, point.x + 25, point.y + 25));
            map_point_center.draw(canvas);
        }

        if (myLocation != null) {
            p.toPixels(myLocation, point);

            Drawable map_point_mylocation = ctx.getResources().getDrawable(ctx.getResources().getIdentifier("center", "drawable", "net.mycitizen.mcn"));
            map_point_mylocation.setBounds(new Rect(point.x - 25, point.y - 50, point.x + 25, point.y));
            map_point_mylocation.draw(canvas);
        }


    }


    public void updateMyLocation(GeoPoint myLocation) {
        this.myLocation = myLocation;

    }

    public void updateCircle(int radius) {
        this.radius = radius;

    }

    public void updateCenter(GeoPoint mapCenter) {
        this.center = mapCenter;

    }


}
