package com.backend.project.entity;

import com.backend.project.exception.ErrorCode;
import com.backend.project.exception.HomeTaxException;
import com.sun.istack.NotNull;
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

    // 필수값 검증 편의 메소드
    public void hashError() throws HomeTaxException {
        System.out.println("singPri.getName().endsWith(\".key\") = " + singPri.getOriginalFilename());
        if (singPri.isEmpty()) {
            throw new HomeTaxException(ErrorCode.SIGN_PRI_NOT_FOUND);
        }else if(singCert.isEmpty()){
            throw new HomeTaxException(ErrorCode.SIGN_CERT_NOT_FOUND);
        }else if(signPassword == null || signPassword.trim().equals("")){
            throw new HomeTaxException(ErrorCode.PASSWORD_NOT_FOUND);
        }else if(!singPri.getOriginalFilename().endsWith(".key")){
            throw new HomeTaxException(ErrorCode.SIGN_PRI_EXTENSION_ERROR);
        }else if(!singCert.getOriginalFilename().endsWith(".der")){
            throw new HomeTaxException(ErrorCode.SIGN_CERT_EXTENSION_ERROR);
        }else if(start_dt == null || start_dt.trim().equals("")){
            throw new HomeTaxException(ErrorCode.SEARCH_START_DATE);
        }else if(end_dt == null || end_dt.trim().equals("")){
            throw new HomeTaxException(ErrorCode.SEARCH_START_DATE);
        }
    }
}
