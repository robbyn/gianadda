package org.tastefuljava.gianadda.domain;

import java.util.Set;
import org.tastefuljava.gianadda.util.Util;

public class Folder {
    private int id;
    private Integer parentId;
    private String name;
    private String title;
    private String description;

    public static Folder getRoot(String name) {
        return CurrentMapper.get().getRootFolder(name);
    }

    public int getId() {
        return id;
    }

    public Folder getParent() {
        return parentId == null ? null : CurrentMapper.get().getFolderById(id);
    }

    public void setParent(Folder parent) {
        this.parentId = parent == null ? null : parent.getId();
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
        CurrentMapper.get().deleteFolder(id);
    }

    public boolean isRoot() {
        return parentId == null;
    }

    public int getLevel() {
        Folder parent = getParent();
        return parent == null ? 0 : parent.getLevel()+1;
    }

    public String getPath() {
        Folder parent = getParent();
        return parent == null || parent.isRoot()
                ? name : parent.getPath() + "/" + name;
    }

    public String getUrlPath() {
        Folder parent = getParent();
        String esc = Util.urlEncode(name);
        return parent == null || parent.isRoot()
                ? esc : parent.getUrlPath() + "/" + esc;
    }

    public Picture getPicture(String name) {
        return CurrentMapper.get().getPictureByName(id, name);
    }

    public Set<Picture> getPictures() {
        return CurrentMapper.get().getFolderPictures(id);
    }

    public Folder getSubfolder(String name) {
        return CurrentMapper.get().getFolderByName(id, name);
    }

    public Set<Folder> getSubfolders() {
        return CurrentMapper.get().getSubfolders(id);
    }
}
