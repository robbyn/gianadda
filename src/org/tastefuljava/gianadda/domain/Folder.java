package org.tastefuljava.gianadda.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.tastefuljava.gianadda.util.Util;

public class Folder {
    private int id;
    private Folder parent;
    private String name;
    private String title;
    private String description;
    private Map<String,Picture> pictures;
    private Map<String,Folder> folders;

    public static Folder getRoot(String name) {
        return CurrentMapper.get().getRootFolder(name);
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
        if (parent != null) {
            parent.folderInserted(this);
        }
    }

    public void update() {
        CurrentMapper.get().updateFolder(this);
        if (parent != null) {
            parent.folderUpdated(this);
        }
    }

    public void delete() {
        CurrentMapper.get().deleteFolder(this);
        if (parent != null) {
            parent.folderDeleted(this);
        }
    }

    public boolean isRoot() {
        return parent == null;
    }

    public int getLevel() {
        return parent == null ? 0 : parent.getLevel()+1;
    }

    public String getPath() {
        return parent == null || parent.isRoot()
                ? name : parent.getPath() + "/" + name;
    }

    public String getUrlPath() {
        String esc = Util.urlEncode(name);
        return parent == null || parent.isRoot()
                ? esc : parent.getUrlPath() + "/" + esc;
    }

    public Picture getPicture(String name) {
        requirePictures();
        return pictures.get(name);
    }

    public List<Picture> getPictures() {
        requirePictures();
        return new ArrayList<>(pictures.values());
    }

    public Folder getSubfolder(String name) {
        requireFolders();
        return folders.get(name);
    }

    public List<Folder> getSubfolders() {
        requireFolders();
        return new ArrayList<>(folders.values());
    }

    private void requirePictures() {
        if (pictures == null) {
            pictures = new TreeMap<>();
            for (Picture pic: CurrentMapper.get().getFolderPictures(this)) {
                pictures.put(pic.getName(), pic);
            }
        }
    }

    void pictureInserted(Picture pic) {
        requirePictures();
        pictures.put(pic.getName(), pic);
    }

    void pictureUpdated(Picture pic) {
    }

    void pictureDeleted(Picture pic) {
        requirePictures();
        pictures.remove(pic.getName());
    }

    private void requireFolders() {
        if (folders == null) {
            folders = new TreeMap<>();
            for (Folder child: CurrentMapper.get().getSubfolders(this)) {
                folders.put(child.getName(), child);
            }
        }
    }

    private void folderInserted(Folder child) {
        requireFolders();
        folders.put(child.getName(), child);
    }

    private void folderUpdated(Folder child) {
    }

    private void folderDeleted(Folder child) {
        requireFolders();
        folders.remove(child.getName());
    }
}
