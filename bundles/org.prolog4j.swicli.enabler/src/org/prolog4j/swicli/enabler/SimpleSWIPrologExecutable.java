package org.prolog4j.swicli.enabler;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.prolog4j.swicli.SWIPrologExecutable;

public class SimpleSWIPrologExecutable implements SWIPrologExecutable {

    private final String path;
    private final Map<String, String> environment = new HashMap<>();

    public SimpleSWIPrologExecutable(File executable, File swiHome) {
        this(executable, swiHome, null);
    }
    
    public SimpleSWIPrologExecutable(File executable, File swiHome, File libDir) {
        super();
        this.path = executable.getAbsolutePath();
        this.environment.put("SWI_HOME", swiHome.getAbsolutePath());
        if (libDir != null) {
            this.environment.put("LD_LIBRARY_PATH", "$LD_LIBRARY_PATH:" + libDir.getAbsolutePath());
        }
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getEnvironment() {
        return environment;
    }

}
