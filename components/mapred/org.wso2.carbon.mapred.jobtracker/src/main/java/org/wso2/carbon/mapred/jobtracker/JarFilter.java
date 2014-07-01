package org.wso2.carbon.mapred.jobtracker;

/**
 * Created by IntelliJ IDEA.
 * User: wathsala
 * Date: 9/14/11
 * Time: 1:39 PM
 * To change this template use File | Settings | File Templates.
 */

import java.io.FilenameFilter;
import java.io.File;

public class JarFilter implements FilenameFilter{
    private final String ext = ".jar";

    public boolean accept(File dir, String name) {
        return name.endsWith(ext);
    }
}

