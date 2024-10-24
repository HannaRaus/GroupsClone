package com.example.microservices.service;

import com.example.microservices.dto.Group;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
class MockGroupApiService implements GroupAPIService {

    //key - groupNumber, value - identifierInGroup to group
    Map<Integer, Map<Integer, Group>> DB;

    @Override
    public List<Group> getByGroupNumber(Integer groupNumber) {
        return new ArrayList<>(DB.getOrDefault(groupNumber, Collections.emptyMap()).values());
    }

    @Override
    public Group create(Group group) {
        Map<Integer, Group> existing = DB.getOrDefault(group.getGroupNumber(), new ConcurrentHashMap<>());
        if (existing.containsKey(group.getIdentifierInGroup())) {
            throw new RuntimeException("Can't create a new group as the is already one with the same groupNumber and identifierInGroup");
        }
        existing.put(group.getIdentifierInGroup(), group);
        DB.put(group.getGroupNumber(), existing);
        return group;
    }

    @Override
    public Group update(Group group) {
        return Optional.ofNullable(DB.get(group.getGroupNumber()))
                .map(existing -> existing.get(group.getIdentifierInGroup()))
                .map(existing -> existing
                        .setProp1(group.getProp1())
                        .setProp2(group.getProp2())
                        .setProp3(group.getProp3()))
                .orElseThrow(() -> new RuntimeException("The group with provided number and identifier doesn't exist"));
    }

    @Override
    public void delete(Group group) {
        Optional.ofNullable(DB.get(group.getGroupNumber()))
                .map(existing -> existing.remove(group.getIdentifierInGroup(), group));
    }
}
