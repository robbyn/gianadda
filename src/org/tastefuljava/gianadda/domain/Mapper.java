package org.tastefuljava.gianadda.domain;

import java.util.Set;

public interface Mapper {
    Picture getPictureById(int id);
    Set<Picture> getFolderPictures(int folderId);
    void insertPicture(Picture pic);
    void updatePicture(Picture pic);
    void deletePicture(int id);

    Folder getFolderById(int id);
    Folder getRootFolder(String name);
    Set<Folder> getSubfolders(int folderId);
    void insertFolder(Folder folder);
    void updateFolder(Folder folder);
    void deleteFolder(int id);
}
