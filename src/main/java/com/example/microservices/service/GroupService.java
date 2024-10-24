package com.example.microservices.service;

import com.example.microservices.dto.Group;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class GroupService {

    GroupAPIService apiService;

    /*
    get all source groups (to clone from) and target groups (to clone to)
    identify what groups to create/update/delete
    verify that function runs smoothly, add exception handling, rollback if needed
     */
    public void clone(Integer sourceGroupNumber, Integer targetGroupNumber) {
        List<Group> sourceGroups = apiService.getByGroupNumber(sourceGroupNumber);
        List<Group> targetGroups = apiService.getByGroupNumber(targetGroupNumber);

        //it is guarantied that identifierInGroup is unique across groups with same groupNumber, those we can use identity function
        Map<Integer, Group> sourceIdentifierToGroup = sourceGroups.stream()
                .collect(Collectors.toMap(Group::getIdentifierInGroup, Function.identity()));
        Map<Integer, Group> targetIdentifierToGroup = targetGroups.stream()
                .collect(Collectors.toMap(Group::getIdentifierInGroup, Function.identity()));

        List<Group> toCreate = getGroupsToCreate(targetGroupNumber, sourceGroups, targetIdentifierToGroup);
        List<Group> toUpdate = getGroupsToUpdate(targetGroupNumber, sourceGroups, targetIdentifierToGroup);
        List<Group> toDelete = getGroupsToDelete(targetGroups, sourceIdentifierToGroup);

        try {
            toCreate.forEach(apiService::create);
            toUpdate.forEach(apiService::update);
            toDelete.forEach(apiService::delete);
        } catch (Exception e) {
            //exception handling - e.g. create method succeeded and update or delete fails
            //can be handled with retry on the Webclient level with some backoff policy
            //rollback all changes and fail the clone request entirely (will cost us additional API calls)
            //we can track all successfully creates/updated/deleted in some ExecutionResult and in case of fail - rollback only successfully executed groups
            throw new RuntimeException("Failed to clone groups", e);
        }
    }

    //define what groups to create (identifierInGroup that are missing in target group list)
    private List<Group> getGroupsToCreate(Integer groupNumber, List<Group> sourceGroups, Map<Integer, Group> targetIdentifierToGroup) {
        return sourceGroups.stream()
                .filter(group -> !targetIdentifierToGroup.containsKey(group.getIdentifierInGroup()))
                .map(group -> group.clone(groupNumber))
                .toList();
    }

    //define what groups to update (identifierInGroup that are present in target group list)
    private static List<Group> getGroupsToUpdate(Integer groupNumber, List<Group> sourceGroups, Map<Integer, Group> targetIdentifierToGroup) {
        return sourceGroups.stream()
                .filter(group -> targetIdentifierToGroup.containsKey(group.getIdentifierInGroup()))
                //filter those groups that have already the same properties to reduce API calls
                .filter(group -> !group.equalsIgnoringGroupNumber(targetIdentifierToGroup.get(group.getIdentifierInGroup())))
                .map(group -> group.clone(groupNumber))
                .toList();
    }

    //define what groups to delete (identifierInGroup that are present in target group list, but missing in a source group list)
    private static List<Group> getGroupsToDelete(List<Group> targetGroups, Map<Integer, Group> sourceIdentifierToGroup) {
        return targetGroups.stream()
                .filter(group -> !sourceIdentifierToGroup.containsKey(group.getIdentifierInGroup()))
                .toList();
    }

}
