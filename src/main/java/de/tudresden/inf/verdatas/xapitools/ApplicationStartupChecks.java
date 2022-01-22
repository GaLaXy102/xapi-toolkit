package de.tudresden.inf.verdatas.xapitools;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class ApplicationStartupChecks {

    public ApplicationStartupChecks(@Value("${xapi.datasim.sim-storage}") String storageDir) throws IOException {
        this.checkStorageDirWritable(storageDir);
    }

    private void checkStorageDirWritable(String storageDir) throws IOException {
        File storageDirFile = new File(storageDir);
        storageDirFile.mkdirs();
        if (!storageDirFile.canWrite()) {
            // We can't write our Data directory
            throw new IOException("Cannot write storage directory.");
        }
    }
}
