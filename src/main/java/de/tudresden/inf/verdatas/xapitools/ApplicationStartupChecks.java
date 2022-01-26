package de.tudresden.inf.verdatas.xapitools;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

/**
 * Component which checks for possible misconfigurations at Boot Time
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
@Component
public class ApplicationStartupChecks {

    /**
     * Checks shall be called on instantiation, so declare any checks in this constructor.
     * You can bind any parameters using the {@link Value}-Annotation.
     *
     * @param storageDir automatically bound to the property "xapi.datasim.sim-storage", Storage Directory of the Datasim Simulations
     * @throws IOException like any exception - marks that at least one check failed
     */
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
