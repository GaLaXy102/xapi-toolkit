package de.tudresden.inf.verdatas.xapitools.datasim.persistence;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * This contains all alignable Element types for Datasim Components as described in https://github.com/yetanalytics/datasim/issues/41
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum DatasimProfileAlignableElementType {
    PATTERN("Pattern"),
    ACTIVITY("ActivityType"),
    TEMPLATE("StatementTemplate");

    @Getter
    private final String value;
}
