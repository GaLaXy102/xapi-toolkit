package de.tudresden.inf.verdatas.xapitools.datasim.persistence;

import lombok.NonNull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.util.Streamable;

import java.util.UUID;

/**
 * Repository to persist {@link DatasimProfile}s
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
public interface DatasimProfileRepository extends CrudRepository<DatasimProfile, UUID> {
    /**
     * Find all Profiles. They are usually created by {@link DatasimProfileSeeder}.
     *
     * @return Streamable of all Profiles in the system
     */
    @Override
    @NonNull
    Streamable<DatasimProfile> findAll();

    /**
     * Check whether a profile exists by its filename
     *
     * @param filename File name to check
     * @return exists -> true, else false
     */
    Boolean existsByFilename(String filename);
}
