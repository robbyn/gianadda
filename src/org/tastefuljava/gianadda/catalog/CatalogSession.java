package org.tastefuljava.gianadda.catalog;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.apache.ibatis.session.SqlSession;
import org.tastefuljava.gianadda.domain.CurrentMapper;
import org.tastefuljava.gianadda.domain.Folder;
import org.tastefuljava.gianadda.domain.Mapper;
import org.tastefuljava.gianadda.domain.Picture;

public class CatalogSession implements Closeable {
    private final SqlSession session;
    private final WeakCache<Integer,Picture> picCache = new WeakCache<>();
    private final WeakCache<Integer,Folder> folderCache = new WeakCache<>();

    CatalogSession(SqlSession session) {
        boolean ok = false;
        this.session = session;
        try {
            Mapper map = session.getMapper(Mapper.class);
            CurrentMapper.set(new Wrapper(map));
            ok = true;
        } finally {
            if (!ok) {
                session.close();
            }
        }
    }

    public void commit() {
        session.commit();
    }

    @Override
    public void close() {
        CurrentMapper.set(null);
        session.close();
    }

    private class Wrapper implements Mapper {
        private final Mapper delegate;

        private Wrapper(Mapper delegate) {
            this.delegate = delegate;
        }

        @Override
        public Picture getPictureById(int id) {
            Picture pic = picCache.get(id);
            if (pic != null) {
                return pic;
            }
            pic = delegate.getPictureById(id);
            if (pic != null) {
                picCache.put(id, pic);
            }
            return pic;
        }

        @Override
        public Picture getPictureByName(int folderId, String name) {
            Picture pic = delegate.getPictureByName(folderId, name);
            return pic == null ? null : picCache.getOrPut(pic.getId(), pic);
        }

        @Override
        public List<Picture> getFolderPictures(int folderId) {
            List<Picture> result = new ArrayList<>();
            for (Picture pic: delegate.getFolderPictures(folderId)) {
                result.add(picCache.getOrPut(pic.getId(), pic));
            }
            return result;
        }

        @Override
        public void insertPicture(Picture pic) {
            delegate.insertPicture(pic);
            picCache.put(pic.getId(), pic);
        }

        @Override
        public void updatePicture(Picture pic) {
            delegate.updatePicture(pic);
        }

        @Override
        public void deletePicture(int id) {
            delegate.deletePicture(id);
            picCache.remove(id);
        }

        @Override
        public Folder getFolderById(int id) {
            Folder folder = folderCache.get(id);
            if (folder != null) {
                return folder;
            }
            folder = delegate.getFolderById(id);
            if (folder != null) {
                folderCache.put(id, folder);
            }
            return folder;
        }

        @Override
        public Folder getFolderByName(int folderId, String name) {
            Folder folder = delegate.getFolderByName(folderId, name);
            return folder == null
                    ? null : folderCache.getOrPut(folder.getId(), folder);
        }

        @Override
        public Folder getRootFolder(String name) {
            Folder folder = delegate.getRootFolder(name);
            return folder == null
                    ? null : folderCache.getOrPut(folder.getId(), folder);
        }

        @Override
        public List<Folder> getSubfolders(int folderId) {
            List<Folder> result = new ArrayList<>();
            for (Folder folder: delegate.getSubfolders(folderId)) {
                result.add(folderCache.getOrPut(folder.getId(), folder));
            }
            return result;
        }

        @Override
        public void insertFolder(Folder folder) {
            delegate.insertFolder(folder);
            folderCache.put(folder.getId(), folder);
        }

        @Override
        public void updateFolder(Folder folder) {
            delegate.updateFolder(folder);
        }

        @Override
        public void deleteFolder(int id) {
            delegate.deleteFolder(id);
            folderCache.remove(id);
        }
    }
}
