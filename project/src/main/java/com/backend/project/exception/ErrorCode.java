package com.backend.project.exception;

import lombok.Getter;
import org.aspectj.weaver.ast.Not;

@Getter
public enum ErrorCode {
    /*
        400 : Bad Request, 요청이 부적절 할 때, 유효성 검증 실패, 필수 값 누락 등.
        401 : Unauthorized, 인증 실패, 로그인하지 않은 사용자 또는 권한 없는 사용자 처리
        402 : Payment Required
        403 : Forbidden, 인증 성공 그러나 자원에 대한 권한 없음. 삭제, 수정시 권한 없음.
        404 : org.aspectj.weaver.ast.Not Found, 요청한 URI에 대한 리소스 없을 때 사용.
        405 : Method Not Allowed, 사용 불가능한 Method를 이용한 경우.
        406 : Not Acceptable, 요청된 리소스의 미디어 타입을 제공하지 못할 때 사용.
        408 : Request Timeout
        409 : Conflict, 리소스 상태에 위반되는 행위 시 사용.
        413 : Payload Too Large
        423 : Locked
        428 : Precondition Required
        429 : Too Many Requests
        500 : 서버 에러
    */
    SUCCESS(200, "200", "success" )
    , BAD_REQUEST(401,"401","BAD_REQUEST")
    , SYSTEM_ERROR(500 , "500", "SYSTEM_ERROR")
    , SIGN_PRI_NOT_FOUND(400 , "H001", "[SIGN_PRI_NOT_FOUND] 공인인증서 signPri.key 파일은 필수입니다.")
    , SIGN_CERT_NOT_FOUND(400 , "H002", "[SIGN_CERT_NOT_FOUND] 공인인증서 signCert.key 파일은 필수입니다.")
    , PASSWORD_NOT_FOUND(400 , "H003", "[PASSWORD_NOT_FOUND] 공인인증서 패스워드는 필수입니다.")
    , SIGN_PRI_EXTENSION_ERROR(400 , "H004", "[SIGN_PRI_EXTENSION_ERROR] 공인인증서 signPir 파일의 확장자가 잘못되었습니다.")
    , SIGN_CERT_EXTENSION_ERROR(400 , "H005", "[SIGN_CERT_EXTENSION_ERROR] 공인인증서 signCert 파일의 확장자가 잘못되었습니다..")
    , PASSWORD_ERROR(400 , "H006", "[PASSWORD_ERROR] 패스워드 검증 오류가 발생하였습니다.")
    , LOGIN_ERROR(400 , "H007", "[LOGIN_ERROR] 로그인 과정중 오류가 발생하였습니다.")
    , DECR_ERROR(400 , "H008", "[FILE_DECR_ERROR] 복호화 과정중 오류가 발생하였습니다.")
    , SEARCH_START_DATE(400 , "H009", "[SEARCH_START_DATE] 조회 시작일은 필수값입니다.")
    , SEARCH_END_DATE(400 , "H010", "[SEARCH_END_DATE] 조회 종료일은 필수값입니다.")
    ;

    private String message;
    private String code;
    private int status;

    private ErrorCode(int states , String code, String message) {
        this.code = code;
        this.message = message;
        this.status = states;
    }

}
