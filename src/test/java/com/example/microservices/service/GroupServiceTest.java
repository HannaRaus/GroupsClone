package com.example.microservices.service;

import com.example.microservices.dto.Group;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;


/**
 * You have an external API third part with CRUD operations to manage objects with the following properties: {groupNumber, identifierInGroup, prop1, prop2, prop3}.
 * The combination of groupNumber and identifierInGroup uniquely identifies an object.
 * <p>
 * Task
 * You need to clone the objects from one group into another group so the second group matches the firs group attributes (except groupNumber). To achieve this, you must make API calls for Create, Update, and Delete (CUD) operations. Please limit the number of API calls as much as possible.
 * <p>
 * Sample input
 * <p>
 * A GET call (with groupNumber = 1) =>
 * <p>
 * [
 * { groupNumber: 1, identifierInGroup: 101, prop1: "A", prop2: "B", prop3: "C" },
 * { groupNumber: 1, identifierInGroup: 102, prop1: "D", prop2: "E", prop3: "F" },
 * ]
 * <p>
 * A GET call (with groupNumber = 2) =>
 * [
 * { groupNumber: 2, identifierInGroup: 101, prop1: "X", prop2: "Y", prop3: "Z" },
 * { groupNumber: 2, identifierInGroup: 203, prop1: "M", prop2: "N", prop3: "O" }
 * ]
 * <p>
 * <p>
 * If we call the implemented clone(groupNumber = 1, groupNumber= 2) the expected call to GET (with groupNumber = 2) should be
 * <p>
 * [
 * { groupNumber: 2, identifierInGroup: 101, prop1: "A", prop2: "B", prop3: "C" },
 * { groupNumber: 2, identifierInGroup: 102, prop1: "D", prop2: "E", prop3: "F" }
 * ]
 */
class GroupServiceTest {

    GroupAPIService apiService = new MockGroupApiService(new ConcurrentHashMap<>());
    GroupService target = new GroupService(apiService);

    @Test
    void clone_happyPath_shouldCreateNewGroupsUpdateExistingDeleteRedundant() {
        Group groupOneId101 = new Group(1, 101, "A", "B", "C");
        Group groupOneId102 = new Group(1, 102, "D", "E", "F");
        Group groupTwoId101 = new Group(2, 101, "X", "Y", "Z");
        Group groupTwoId203 = new Group(2, 203, "M", "N", "O");
        Stream.of(groupOneId101, groupOneId102, groupTwoId101, groupTwoId203).forEach(apiService::create);

        Group expectedCreatedGroup = new Group(2, 102, "D", "E", "F");
        Group expectedUpdatedGroup = new Group(2, 101, "A", "B", "C");

        target.clone(1, 2);

        List<Group> actualSourceGroups = apiService.getByGroupNumber(1);
        List<Group> actualTargetGroups = apiService.getByGroupNumber(2);

        Assertions.assertThat(actualSourceGroups).containsExactly(groupOneId101, groupOneId102);
        Assertions.assertThat(actualTargetGroups).containsOnly(expectedCreatedGroup, expectedUpdatedGroup);
        Assertions.assertThat(actualTargetGroups).doesNotContain(groupTwoId203);
    }

}
