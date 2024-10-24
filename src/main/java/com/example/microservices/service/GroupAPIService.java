package com.example.microservices.service;


import com.example.microservices.dto.Group;

import java.util.List;

/**
 * Assuming the following external API
 * get groups GET /groups?groupNumber={groupNumber}
 * create group POST /groups
 * update group PUT /groups?groupNumber={groupNumber}&identifierInGroup={$identifierInGroup}
 * delete group DELETE /groups?groupNumber={groupNumber}&identifierInGroup={$identifierInGroup}
 */
public interface GroupAPIService {

    List<Group> getByGroupNumber(Integer groupNumber);

    Group create(Group group);

    Group update(Group group);

    void delete(Group group);
}
