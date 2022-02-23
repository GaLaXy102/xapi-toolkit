package de.tudresden.inf.verdatas.xapitools.dave;

import de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveGraphDescription;
import de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveQuery;
import de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveVis;
import de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveVisRepository;
import org.springframework.stereotype.Component;

@Component
public class DaveVisSeeder {
    private final DaveVisRepository daveVisRepository;

    /**
     * This class is instantiated by Spring Boot and not intended for manual creation.
     */
    DaveVisSeeder(DaveVisRepository daveVisRepository) {
        this.daveVisRepository = daveVisRepository;
        this.seed();
    }

    /**
     * Create some connection samples
     */
    private void seed() {
        try {
            if (this.daveVisRepository.count() == 0) {
                DaveVis vis = this.daveVisRepository.save( new DaveVis("Learners Engagement", new DaveQuery("Learners Engagement",
                        "[:find (count ?s) ?c\n" +
                        "  :where\n" +
                        "  [?s :statement/actor ?a]\n" +
                        "  [?a :agent/mbox ?c]]"),
                        new DaveGraphDescription("Top 10 DESC",
                                "{\n" +
                                        "  \"$schema\": \"https://vega.github.io/schema/vega/v5.json\",\n" +
                                        "  \"width\": 400,\n" +
                                        "  \"height\": 200,\n" +
                                        "  \"padding\": 5,\n" +
                                        "\n" +
                                        "  \"data\": [\n" +
                                        "    {      \n" +
                                        "      \"name\": \"table\",\n" +
                                        "      \"source\": \"result\",\n" +
                                        "      \"transform\": [\n" +
                                        "        { \"type\": \"collect\", \"sort\": {\"field\": \"count_?s\", \"order\" : \"descending\"} },\n" +
                                        "        {\n" +
                                        "          \"type\": \"window\",\n" +
                                        "          \"sort\": {\"field\": \"count_?s\", \"order\": \"descending\"},\n" +
                                        "          \"ops\": [\"rank\"],\n" +
                                        "          \"fields\": [null],\n" +
                                        "          \"as\": [\"rank\"]\n" +
                                        "        },\n" +
                                        "        \n" +
                                        "        { \"type\": \"filter\", \"expr\": \"datum.rank < 11\"}\n" +
                                        "      ]\n" +
                                        "    }\n" +
                                        "  ],\n" +
                                        "\n" +
                                        "  \"signals\": [\n" +
                                        "    {\n" +
                                        "      \"name\": \"tooltip\",\n" +
                                        "      \"value\": {},\n" +
                                        "      \"on\": [\n" +
                                        "        {\"events\": \"rect:mouseover\", \"update\": \"datum\"},\n" +
                                        "        {\"events\": \"rect:mouseout\",  \"update\": \"{}\"}\n" +
                                        "      ]\n" +
                                        "    }\n" +
                                        "  ],\n" +
                                        "\n" +
                                        "  \"scales\": [\n" +
                                        "    {\n" +
                                        "      \"name\": \"xscale\",\n" +
                                        "      \"type\": \"band\",\n" +
                                        "      \"domain\": {\"data\": \"table\", \"field\": \"?c\"},\n" +
                                        "      \"range\": \"width\",\n" +
                                        "      \"padding\": 0.05,\n" +
                                        "      \"round\": true\n" +
                                        "    },\n" +
                                        "    {\n" +
                                        "      \"name\": \"yscale\",\n" +
                                        "      \"domain\": {\"data\": \"table\", \"field\": \"count_?s\"},\n" +
                                        "      \"nice\": true,\n" +
                                        "      \"range\": \"height\"\n" +
                                        "    }\n" +
                                        "  ],\n" +
                                        "\n" +
                                        "  \"axes\": [\n" +
                                        "    { \"orient\": \"bottom\", \"scale\": \"xscale\", \"labelAngle\": -35, \"zindex\": 2 },\n" +
                                        "    { \"orient\": \"left\", \"scale\": \"yscale\" }\n" +
                                        "  ],\n" +
                                        "\n" +
                                        "  \"marks\": [\n" +
                                        "    {\n" +
                                        "      \"type\": \"rect\",\n" +
                                        "      \"from\": {\"data\":\"table\"},\n" +
                                        "      \"encode\": {\n" +
                                        "        \"enter\": {\n" +
                                        "          \"x\": {\"scale\": \"xscale\", \"field\": \"?c\"},\n" +
                                        "          \"width\": {\"scale\": \"xscale\", \"band\": 1},\n" +
                                        "          \"y\": {\"scale\": \"yscale\", \"field\": \"count_?s\"},\n" +
                                        "          \"y2\": {\"scale\": \"yscale\", \"value\": 0}\n" +
                                        "        },\n" +
                                        "        \"update\": {\n" +
                                        "          \"fill\": {\"value\": \"steelblue\"}\n" +
                                        "        },\n" +
                                        "        \"hover\": {\n" +
                                        "          \"fill\": {\"value\": \"red\"}\n" +
                                        "        }\n" +
                                        "      }\n" +
                                        "    }\n" +
                                        "  ]\n" +
                                        "}"), true));
                this.daveVisRepository.save(vis);
            }
        } catch (Exception ignored) {
        }
    }
}
