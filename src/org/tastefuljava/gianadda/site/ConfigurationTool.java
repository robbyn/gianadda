package org.tastefuljava.gianadda.site;

import java.awt.Dimension;
import java.util.Map;
import org.tastefuljava.gianadda.util.Configuration;
import org.tastefuljava.gianadda.util.QuickMap;

public class ConfigurationTool extends QuickMap<String,String> {
    private final Configuration conf;

    ConfigurationTool(Configuration conf) {
        this.conf = conf;
    }

    @Override
    public String get(Object key) {
        if (key instanceof String) {
            return conf.getString((String)key, null);
        } else {
            return null;
        }
    }

    public Map<String,Integer> getInts() {
        return new QuickMap<String,Integer>() {
            @Override
            public Integer get(Object key) {
                if (key instanceof String) {
                    return conf.getInt((String)key, 0);
                } else {
                    return 0;
                }
            }
        };
    }

    public Map<String,Boolean> getBools() {
        return new QuickMap<String,Boolean>() {
            @Override
            public Boolean get(Object key) {
                if (key instanceof String) {
                    return conf.getBoolean((String)key, false);
                } else {
                    return false;
                }
            }
        };
    }

    public Map<String,Dimension> getDims() {
        return new QuickMap<String,Dimension>() {
            @Override
            public Dimension get(Object key) {
                if (key instanceof String) {
                    return conf.getDimension((String)key, null);
                } else {
                    return null;
                }
            }
        };
    }
}
