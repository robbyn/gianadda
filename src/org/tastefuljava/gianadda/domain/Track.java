package org.tastefuljava.gianadda.domain;

import org.tastefuljava.gianadda.geo.TrackPoint;
import java.util.Date;

public class Track {
    private int id;
    private Folder folder;
    private String name;
    private Date dateTime;
    private TrackPoint[] points = {};

    public int getId() {
        return id;
    }

    public Folder getFolder() {
        return folder;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public void setPoints(TrackPoint[] newValue) {
        int count = newValue == null ? 0 : newValue.length;
        points = new TrackPoint[count];
        for (int i = 0; i < count; ++i) {
            points[i] = newValue[i];
        }
    }

    public TrackPoint[] getPoints() {
        return points.clone();
    }
}
