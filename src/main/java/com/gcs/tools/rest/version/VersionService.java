package com.gcs.tools.rest.version;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

@Slf4j
public class VersionService
{
    public static class GetVersionHelper
    {
        public static final VersionService _instance = new VersionService();
    }





    public static VersionService getInstance()
    {
        return GetVersionHelper._instance;
    }





    public String getVersion()
    {
        var vfile = getClass().getResourceAsStream("/version.txt");
        if (vfile != null)
        {
            try
            {
                return new String(vfile.readAllBytes());
            }
            catch (IOException ex_)
            {
                _logger.error(ex_.toString(), ex_);
                return "ERROR: " + ex_.toString();
            }
        }

        try
        {
            Class clazz = VersionService.class;
            String className = clazz.getSimpleName() + ".class";
            String classPath = clazz.getResource(className).toString();
            if (!classPath.startsWith("jar"))
            {
                // Class not from JAR
                return "UNK";
            }
            String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1)
                + "/META-INF/MANIFEST.MF";
            Manifest manifest = new Manifest(new URL(manifestPath).openStream());
            Attributes attr = manifest.getMainAttributes();
            return attr.getValue("Specification-Version");
        }
        catch (Exception ex_)
        {
            _logger.error(ex_.toString(), ex_);
        }

        return getClass().getPackage().getImplementationVersion();
    }

}
