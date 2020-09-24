package com.snap.pactconsumerdemo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
@Builder(toBuilder = true)
@JsonDeserialize(builder = DataModel.DataModelBuilder.class)
public class DataModel {

    @JsonProperty("name")
    private final String name;
    @JsonProperty("price")
    private final double price;
}
