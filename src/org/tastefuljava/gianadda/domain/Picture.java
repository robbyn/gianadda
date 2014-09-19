package org.tastefuljava.gianadda.domain;

import java.awt.Dimension;
import java.util.Date;
import org.tastefuljava.jedo.Ref;
import org.tastefuljava.jedo.SimpleRef;

public class Picture {
    private int id;
    private Ref<Folder> folder = new SimpleRef<>();
    private String name;
    private Date dateTime;
    private int width;
    private int height;
    private GpsData gpsData;
    private String description;
    private String artist;
    private String copyright;

    public int getId() {
        return id;
    }

    public Folder getFolder() {
        return folder.get();
    }

    public void setFolder(Folder folder) {
        this.folder.set(folder);
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

    public Dimension getSize() {
        return new Dimension(width, height);
    }

    public GpsData getGpsData() {
        return gpsData;
    }

    public void setGpsData(GpsData gpsData) {
        this.gpsData = gpsData;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    public String getPath() {
        return getFolder().isRoot()
                ? name : getFolder().getPath() + "/" + name;
    }

    public void insert() {
        CurrentMapper.get().insertPicture(this);
    }

    public void update() {
        CurrentMapper.get().updatePicture(this);
    }

    public void delete() {
        CurrentMapper.get().deletePicture(this);
    }
}
