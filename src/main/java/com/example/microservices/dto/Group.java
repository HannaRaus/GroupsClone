package com.example.microservices.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.util.Objects;

import static lombok.AccessLevel.PRIVATE;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@FieldDefaults(level = PRIVATE)
public class Group {

    Integer groupNumber;
    Integer identifierInGroup;
    String prop1;
    String prop2;
    String prop3;

    //we can use mapstruct instead
    //if we are dealing with external API - we don't need to clone, just update the groupNumber
    public Group clone(Integer newGroupNumber) {
        return new Group()
                .setGroupNumber(newGroupNumber)
                .setIdentifierInGroup(this.identifierInGroup)
                .setProp1(this.prop1)
                .setProp2(this.prop2)
                .setProp3(this.prop3);
    }

    public boolean equalsIgnoringGroupNumber(Group another) {
        return Objects.nonNull(another)
                && Objects.equals(this.identifierInGroup, another.identifierInGroup)
                && Objects.equals(this.prop1, another.prop1)
                && Objects.equals(this.prop2, another.prop2)
                && Objects.equals(this.prop3, another.prop3);
    }
}
