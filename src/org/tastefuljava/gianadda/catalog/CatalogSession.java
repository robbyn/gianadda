package org.tastefuljava.gianadda.catalog;

import java.io.Closeable;
import java.util.List;
import org.tastefuljava.gianadda.domain.CurrentMapper;
import org.tastefuljava.gianadda.domain.Folder;
import org.tastefuljava.gianadda.domain.Mapper;
import org.tastefuljava.gianadda.domain.Picture;
import org.tastefuljava.jedo.Session;

public class CatalogSession implements Closeable {
    private final Session session;

    CatalogSession(Session session) {
        boolean ok = false;
        this.session = session;
        try {
            CurrentMapper.set(new Wrapper());
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
        private Wrapper() {
        }

        @Override
        public Picture getPictureById(int id) {
            return session.load(Picture.class, id);
        }

        @Override
        public void insertPicture(Picture pic) {
            session.insert(pic);
        }

        @Override
        public void updatePicture(Picture pic) {
            session.update(pic);
        }

        @Override
        public void deletePicture(Picture pic) {
            session.delete(pic);
        }

        @Override
        public Folder getFolderById(int id) {
            return session.load(Folder.class, id);
        }

        @Override
        public Folder getRootFolder(String name) {
            return session.queryOne(Folder.class, "root", name);
        }

        @Override
        public void insertFolder(Folder folder) {
            session.insert(folder);
        }

        @Override
        public void updateFolder(Folder folder) {
            session.update(folder);
        }

        @Override
        public void deleteFolder(Folder folder) {
            session.delete(folder);
        }
    }
}
