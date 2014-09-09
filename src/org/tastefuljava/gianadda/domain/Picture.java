package org.tastefuljava.gianadda.domain;

import java.util.Date;

public class Picture {
    private int id;
    private Folder folder;
    private String name;
    private Date dateTime;
    private int width;
    private int height;
    private GpsData gpsData;

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

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public GpsData getGpsData() {
        return gpsData;
    }

    public void setGpsData(GpsData gpsData) {
        this.gpsData = gpsData;
    }

    public String getPath() {
        return folder == null || folder.isRoot()
                ? name : folder.getPath() + "/" + name;
    }

    public void insert() {
        CurrentMapper.get().insertPicture(this);
        folder.pictureAdded(this);
    }

    public void update() {
        CurrentMapper.get().updatePicture(this);
    }

    public void delete() {
        folder.pictureRemoved(this);
        CurrentMapper.get().deletePicture(id);
    }
}
