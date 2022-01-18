package de.tudresden.inf.rn.xapi.datatools.datasim.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.logging.Logger;

public class DatasimProfileAlignableElementHelper {

    private static final String CONCEPTS_NODE_ID = "concepts";
    private static final String TEMPLATES_NODE_ID = "templates";
    private static final String PATTERNS_NODE_ID = "patterns";

    private static final List<String> ALIGNABLE_FIELDS = List.of(CONCEPTS_NODE_ID, TEMPLATES_NODE_ID, PATTERNS_NODE_ID);

    private static final String TYPE_NODE_ID = "type";
    private static final String ID_NODE_ID = "id";

    public static Map<DatasimProfileAlignableElementType, List<URL>> calculatePossibleAlignments(DatasimProfileTO profile) {
        Map<DatasimProfileAlignableElementType, List<URL>> out = new TreeMap<>();
        JsonMapper objectMapper = new JsonMapper();
        JsonNode root;
        try {
            root = objectMapper.readTree(new ClassPathResource("xapi/profiles/" + profile.getFilename()).getFile());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        // For now, this doesn't induce too huge performance drawbacks.
        ALIGNABLE_FIELDS.forEach((key) -> retrieveAlignableComponents(root, key, out));
        return out;
    }

    private static void retrieveAlignableComponents(JsonNode root, String key, Map<DatasimProfileAlignableElementType, List<URL>> out) {
        for (JsonNode currNode : root.findPath(key)) {
            DatasimProfileAlignableElementType type = Optional.ofNullable(currNode.get(TYPE_NODE_ID))
                    .map(JsonNode::textValue)
                    .map((nodeType) -> {
                        for (DatasimProfileAlignableElementType possibleType : DatasimProfileAlignableElementType.values()) {
                            if (possibleType.getValue().equals(nodeType)) return possibleType;
                        }
                        return null;
                    }).orElse(null);
            if (type != null) {
                List<URL> appendable = out.getOrDefault(type, new LinkedList<>());
                try {
                    appendable.add(new URL(currNode.get(ID_NODE_ID).textValue()));
                } catch (MalformedURLException e) {
                    Logger.getLogger(DatasimProfileAlignableElementHelper.class.getName()).warning("Dropped profile entry " + currNode);
                }
                out.put(type, appendable);
            }
        }
    }
}
