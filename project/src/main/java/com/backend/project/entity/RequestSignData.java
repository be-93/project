package com.backend.project.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter @Setter
@RequiredArgsConstructor
public class RequestSignData {

    // 공인인증서 개인키 파일
    private MultipartFile singPri;

    // 공인인증서 공개키 파일
    private MultipartFile singCert;

    // 공인인증서 패스워드
    private String signPassword;

    // 홈택스 스크래핑할 타입 [TEET] 전자세금계산서 / [TECR] 현금영수증
    private String scrapType;

    // 검색 기간
    private String start_dt;
    private String end_dt;

}
