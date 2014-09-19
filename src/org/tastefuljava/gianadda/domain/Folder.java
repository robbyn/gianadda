package org.tastefuljava.gianadda.domain;

import java.util.ArrayList;
import java.util.List;
import org.tastefuljava.gianadda.util.Util;
import org.tastefuljava.jedo.Ref;

public class Folder {
    private int id;
    private final Ref<Folder> parent = new Ref<>();
    private String name;
    private String title;
    private String description;
    private final List<Picture> pictures = new ArrayList<>();
    private final List<Folder> folders = new ArrayList<>();

    public static Folder getRoot(String name) {
        return CurrentMapper.get().getRootFolder(name);
    }

    public int getId() {
        return id;
    }

    public Folder getParent() {
        return parent.get();
    }

    public void setParent(Folder parent) {
        this.parent.set(parent);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        CurrentMapper.get().insertFolder(this);
    }

    public void update() {
        CurrentMapper.get().updateFolder(this);
    }

    public void delete() {
        CurrentMapper.get().deleteFolder(this);
    }

    public boolean isRoot() {
        return getParent() == null;
    }

    public int getLevel() {
        return isRoot() ? 0 : getParent().getLevel()+1;
    }

    public String getPath() {
        return isRoot() || getParent().isRoot()
                ? name : getParent().getPath() + "/" + name;
    }

    public String getUrlPath() {
        String esc = Util.urlEncode(name);
        return isRoot() || getParent().isRoot()
                ? esc : getParent().getUrlPath() + "/" + esc;
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
}
