package com.study.springboot.dto.notice;

import com.study.springboot.entity.NoticeEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class NoticeSaveRequestDto {
    private String noticeType;
    private String noticeTitle;
    private String noticeContent;
    private String noticeImageUrl;

    @Builder
    public NoticeSaveRequestDto(String noticeType, String noticeTitle, String noticeContent, String noticeImageUrl) {
        this.noticeType = noticeType;
        this.noticeTitle = noticeTitle;
        this.noticeContent = noticeContent;
        this.noticeImageUrl = noticeImageUrl;
    }

    public NoticeEntity toEntity(){
        return NoticeEntity.builder()
                .noticeType(noticeType)
                .noticeTitle(noticeTitle)
                .noticeContent(noticeContent)
                .noticeImageUrl(noticeImageUrl)
                .build();
    }
}
