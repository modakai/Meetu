package com.sakura.meetu.enums;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author sakura
 * @date 2023/7/16 18:18:01 周日
 */
@Slf4j
public enum GenderEnum {
    MAN("男"),
    WOMAN("女"),
    UNKNOWN("未知");
    private final String value;

    GenderEnum(String value) {
        this.value = value;
    }

    public static Optional<String> getValue(String type) {
        try {
            GenderEnum[] genderEnums = values();
            GenderEnum genderEnum = Arrays.stream(genderEnums)
                    .filter(item -> item.value.equals(type)).findFirst().get();

            return Optional.ofNullable(genderEnum.value);
        } catch (IllegalArgumentException e) {
            log.info("用户传递性别类型有误: {}", type, e);
            return Optional.ofNullable(null);
        }

    }

    @Override
    public String toString() {
        return value;
    }
}
