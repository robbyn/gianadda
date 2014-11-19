package org.tastefuljava.gianadda.domain;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.tastefuljava.gianadda.util.Util;

public class Folder {
    private int id;
    private Folder parent;
    private String name;
    private Date dateTime;
    private String title;
    private String description;
    private final List<Folder> folders = new ArrayList<>();
    private final List<Picture> pictures = new ArrayList<>();
    private final List<Track> tracks = new ArrayList<>();
    private final List<Tag> tags = new ArrayList<>();

    public static Folder getRoot(String name) {
        return CurrentMapper.get().getRootFolder(name);
    }

    public Folder() {
    }

    public int getId() {
        return id;
    }

    public Folder getParent() {
        return parent;
    }

    public void setParent(Folder parent) {
        this.parent = parent;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void insert() {
        CurrentMapper.get().insert(this);
    }

    public void update() {
        CurrentMapper.get().update(this);
    }

    public void delete() {
        removeAllTags();
        CurrentMapper.get().delete(this);
    }

    public boolean isRoot() {
        return getParent() == null;
    }

    public int getLevel() {
        return isRoot() ? 0 : getParent().getLevel()+1;
    }

    public String getPath() {
        return isRoot() ? "" : getParent().getPath() + name + "/";
    }

    public File getFile(File root) {
        return isRoot() ? root : new File(getParent().getFile(root), name);
    }

    public String getUrlPath() {
        String esc = Util.urlEncode(name);
        return isRoot() || getParent().isRoot()
                ? esc : getParent().getUrlPath() + "/" + esc;
    }

    public Folder getSubfolder(String name) {
        for (Folder child: folders) {
            if (child.getName().equals(name)) {
                return child;
            }
        }
        return null;
    }

    public List<Folder> getSubfolders() {
        return new ArrayList<>(folders);
    }

    public Picture getPicture(String name) {
        for (Picture pic: pictures) {
            if (pic.getName().equals(name)) {
                return pic;
            }
        }
        return null;
    }

    public List<Picture> getPictures() {
        return new ArrayList<>(pictures);
    }

    public Track getTrack(String name) {
        for (Track track: tracks) {
            if (track.getName().equals(name)) {
                return track;
            }
        }
        return null;
    }

    public List<Track> getTracks() {
        return new ArrayList<>(tracks);
    }

    public List<Tag> getTags() {
        return new ArrayList<>(tags);
    }

    public void removeAllTags() {
        CurrentMapper.get().apply(this, "removeAllTags");
    }

    public void addTag(Tag tag) {
        CurrentMapper.get().apply(this, "addTag", tag);
    }

    public void addTag(String label) {
        addTag(Tag.findOrCreate(label));
    }

    public void removeTag(Tag tag) {
        CurrentMapper.get().apply(this, "removeTag", tag);
    }
}
