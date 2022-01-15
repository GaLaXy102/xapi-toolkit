package de.tudresden.inf.rn.xapi.datatools.datasim.persistence;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.util.Pair;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Validated
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DatasimSimulationTO {
    @Getter
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Optional<UUID> id;

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Optional<String> remark;

    @Getter
    @Setter
    @JsonProperty("personae-array")
    private Set<DatasimPersonaGroupTO> personaGroups;

    @Getter
    @Setter
    private Set<ActorWithAlignmentsTO> alignments;

    @Getter
    @Setter
    private DatasimSimulationParamsTO parameters;

    @Getter
    @Setter
    private List<DatasimProfileTO> profiles;

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Optional<Boolean> finalized;

    private static Map<DatasimActor, Set<DatasimAlignmentTO>> mapAlignsFromEntity(Map<DatasimAlignment, DatasimPersona> entityAligns) {
        Map<DatasimPersona, DatasimActor> actorConversion = new HashMap<>();
        entityAligns.values()
                .stream()
                .distinct()
                .map((pers) -> Pair.of(pers, DatasimPersonaTO.of(pers)))
                .forEach((pair) -> actorConversion.put(pair.getFirst(), pair.getSecond()));
        Map<DatasimActor, Set<DatasimAlignmentTO>> out = new HashMap<>();
        entityAligns.forEach((alignment, persona) -> {
            DatasimActor actor = actorConversion.get(persona);
            Set<DatasimAlignmentTO> outAlignments = out.getOrDefault(actor, new HashSet<>());
            outAlignments.add(DatasimAlignmentTO.of(alignment));
            out.put(actor, outAlignments);
        });
        return out;
    }

    private static Map<DatasimAlignment, DatasimPersona> mapAlignsFromTO(Map<DatasimActor, Set<DatasimAlignmentTO>> dtoAligns) {
        Map<DatasimAlignment, DatasimPersona> out = new HashMap<>();
        dtoAligns.forEach((actor, alignmentSet) -> {
            if (actor.getType().equals(DatasimActorType.AGENT)) {
                alignmentSet.forEach((align) -> out.put(align.toExistingDatasimAlignment(), ((DatasimPersonaTO) actor).toExistingDatasimPersona()));
            } else if (actor.getType().equals(DatasimActorType.GROUP)) {
                Set<DatasimPersona> personasFromGroup = ((DatasimPersonaGroupTO) actor).toExistingPersonaGroup().getMember();
                alignmentSet.forEach((align) -> personasFromGroup.forEach(persona -> out.put(align.toExistingDatasimAlignment(), persona)));
            }
        });
        return out;
    }

    public static DatasimSimulationTO of(DatasimSimulation simulation) {
        Set<DatasimPersonaGroupTO> personae = simulation.getPersonaGroups().stream().map(DatasimPersonaGroupTO::of).collect(Collectors.toSet());
        Map<DatasimActor, Set<DatasimAlignmentTO>> aligns = DatasimSimulationTO.mapAlignsFromEntity(simulation.getAlignments());
        return new DatasimSimulationTO(
                Optional.of(simulation.getId()),
                Optional.of(simulation.getRemark()),
                personae,
                aligns.entrySet().stream().map((entry) -> ActorWithAlignmentsTO.with(entry.getKey(), entry.getValue())).collect(Collectors.toSet()),
                DatasimSimulationParamsTO.of(simulation.getParameters()),
                simulation.getProfiles().stream().map(DatasimProfileTO::of).toList(),
                Optional.of(simulation.isFinalized())
        );
    }

    public DatasimSimulationTO forExport() {
        return new DatasimSimulationTO(
                Optional.empty(),
                Optional.empty(),
                this.personaGroups.stream().map(DatasimPersonaGroupTO::forExport).collect(Collectors.toSet()),
                this.alignments.stream().map(ActorWithAlignmentsTO::forExport).collect(Collectors.toSet()),
                this.parameters.forExport(),
                this.profiles,
                Optional.empty()
        );
    }
}
