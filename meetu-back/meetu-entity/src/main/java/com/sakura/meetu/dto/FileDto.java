package com.sakura.meetu.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author sakura
 * @date 2023/9/2 21:21:52 周六
 */
@Setter
@Getter
@EqualsAndHashCode
public class FileDto {

    private Integer simpleFileId;
    private String simpleFileUrl;

    private List<Integer> fileIds;
    private List<String> fileUrls;

}
