package com.example.knowyourcity;

import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapView;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polygon;

import java.util.List;

public class CirclePlottingOverlay extends Overlay {

    private float distanceMeters;
    private GeoPoint point;
    public Polygon p;

    public CirclePlottingOverlay(float distanceMeters, GeoPoint pt, MapView mapView)
    {
        super();
        this.distanceMeters = distanceMeters;
        point=pt;

        if(mapView!=null)
        {
             List<GeoPoint> circle = Polygon.pointsAsCircle(point, distanceMeters);

            p = new Polygon(mapView);
            p.setPoints(circle);
            p.setTitle("A circle");
            p.setOnClickListener(new Polygon.OnClickListener() {
                @Override
                public boolean onClick(Polygon polygon, MapView mapView, GeoPoint eventPos) {
                    return false;
                }
            });
            Paint paint = p.getFillPaint();
            paint.setARGB(50,0,0,255);
            Paint outline = p.getOutlinePaint();
            outline.setStrokeWidth(0);

            mapView.getOverlays().add(0, p);
            mapView.invalidate();
        }

    }

    public boolean isInside(IGeoPoint location)
    {
        return point.distanceToAsDouble(location) <= distanceMeters;
    }


}