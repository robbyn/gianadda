package org.tastefuljava.gianadda.domain;

import java.awt.Dimension;
import java.io.File;
import java.util.Date;
import org.tastefuljava.gianadda.geo.LatLng;
import org.tastefuljava.gianadda.util.Util;

public class Picture {
    private int id;
    private Folder folder;
    private String name;
    private Date dateTime;
    private long fileSize;
    private int width;
    private int height;
    private Integer angle;
    private GpsData gpsData;
    private String description;
    private String artist;
    private String copyright;

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

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
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

    public int getAngle() {
        return angle == null ? 0 : angle;
    }

    public void setAngle(int angle) {
        this.angle = angle;
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

    public String getDescriptionOrName() {
        return Util.isBlank(description) ? name : description;
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
        return getFolder().getPath() + name;
    }

    public File getFile(File root) {
        return new File(getFolder().getFile(root), name);
    }

    public String getUrl() {
        String url = getFolder().getUrl();
        if (!url.endsWith("/")) {
            url += "/";
        }
        return url + Util.urlEncode(name) + "/";
    }

    public LatLng getLocation() {
        return gpsData == null ? null : gpsData.getLocation();
    }

    public void insert() {
        CurrentMapper.get().insert(this);
    }

    public void update() {
        CurrentMapper.get().update(this);
    }

    public void delete() {
        CurrentMapper.get().delete(this);
    }
}
