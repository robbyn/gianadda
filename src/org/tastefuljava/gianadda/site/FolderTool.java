package org.tastefuljava.gianadda.site;

import java.util.List;
import org.tastefuljava.gianadda.domain.Folder;
import org.tastefuljava.gianadda.util.Configuration;

public class FolderTool {
    private final Configuration conf;

    FolderTool(Configuration conf) {
        this.conf = conf;
    }

    public Folder getRoot() {
        return Folder.getRoot("/");
    }

    public List<Folder> latest(int count) {
        return Folder.latest(count);
    }
}
