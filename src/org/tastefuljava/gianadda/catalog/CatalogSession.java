package org.tastefuljava.gianadda.catalog;

import java.io.Closeable;
import java.util.List;
import org.tastefuljava.gianadda.domain.CurrentMapper;
import org.tastefuljava.gianadda.domain.Folder;
import org.tastefuljava.gianadda.domain.Mapper;
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
        public void insert(Object obj) {
            session.insert(obj);
        }

        @Override
        public void update(Object obj) {
            session.update(obj);
        }

        @Override
        public void delete(Object obj) {
            session.delete(obj);
        }

        @Override
        public Folder getRootFolder(String name) {
            return session.queryOne(Folder.class, "root", name);
        }

        @Override
        public <T> T queryOne(Class<T> clazz, String name, Object... parms) {
            return session.queryOneA(clazz, name, parms);
        }

        @Override
        public <T> List<T> query(Class<T> clazz, String name, Object... parms) {
            return session.queryA(clazz, name, parms);
        }

        @Override
        public void invoke(Class<?> clazz, String name, Object... parms) {
            session.invokeA(clazz, name, parms);
        }

        @Override
        public void apply(Object obj, String name, Object... parms) {
            session.applyA(obj, name, parms);
        }
    }
}
