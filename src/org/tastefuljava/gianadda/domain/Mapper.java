package org.tastefuljava.gianadda.domain;

import java.util.Set;
import org.apache.ibatis.annotations.Param;

public interface Mapper {
    Picture getPictureById(int id);
    Picture getPictureByName(@Param("folderId") int folderId,
            @Param("name") String name);
    Set<Picture> getFolderPictures(int folderId);
    void insertPicture(Picture pic);
    void updatePicture(Picture pic);
    void deletePicture(int id);

    Folder getFolderById(int id);
    Folder getFolderByName(@Param("folderId") int folderId,
            @Param("name") String name);
    Folder getRootFolder(String name);
    Set<Folder> getSubfolders(int folderId);
    void insertFolder(Folder folder);
    void updateFolder(Folder folder);
    void deleteFolder(int id);
}
