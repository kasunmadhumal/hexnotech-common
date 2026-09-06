package com.hexnotech.commons.type.comparison;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

import com.hexnotech.commons.util.HexnotechConstant;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "recordName")
public class FieldDelta {
    private String recordId;
    private String recordName;
    private String previousValue;
    private String newValue;

    public static FieldDelta of(String previousValue, String newValue) {
        return FieldDelta.builder().previousValue(previousValue).newValue(newValue).build();
    }

    public static FieldDelta newDelta(String recordName, String newValue) {
        return FieldDelta.builder()
                .recordName(recordName).previousValue(HexnotechConstant.Symbol.EMPTY).newValue(newValue)
                .build();
    }

    public static FieldDelta removedDelta(String recordName, String previousValue) {
        return FieldDelta.builder()
                .recordName(recordName).previousValue(previousValue).newValue(HexnotechConstant.Symbol.EMPTY)
                .build();
    }
}
