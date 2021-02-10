package com.backend.project.api;

import com.backend.project.entity.RequestSignData;
import com.backend.project.exception.CustomExceptionHandler;
import com.backend.project.scrapUtil.HomeTaxLogin;
import com.backend.project.scrapUtil.SignDecr;
import com.backend.project.service.ScrapService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
public class HomeTaxApiController extends CustomExceptionHandler {

    private final ScrapService service;

    // 스크래핑 데이터를 저장하지 않고 직접 데이터 받아오기
    @RequestMapping(value = "/api/v1/scrap" , method = RequestMethod.POST)
    public Result scrapV1(RequestSignData request) throws Exception {
        List<HashMap<String, Object>> result = service.scrapDirectCall(request);
        return new Result(200 ,result.stream().map(ScrapDataDto::new).collect(Collectors.toList()));
    }

    // 스크래핑 데이터를 데이터베이스에 저장 후 데이터 받아오기
    @RequestMapping(value = "/api/v2/scrap" , method = RequestMethod.POST)
    public Result scrapV2(RequestSignData request) throws Exception {
        List<HashMap<String, Object>> result = service.scrapV2(request);
        return new Result(200 , result.stream().map(ScrapDataDto::new).collect(Collectors.toList()));
    }

    @Data
    @AllArgsConstructor
    static class Result<T> {
        private int status;
        private T data;
    }

    // 리턴해줄 데이터 셋
    @Data
    public class ScrapDataDto {

        private String incDdcNm;
        private String mdxpsDdcYn;
        private String statusValue;
        private String ddcYn;
        private Integer cshptTrsTypeCd;
        private String mrntTxprNm;
        private String cshptTrsTypeNm;
        private Integer cshptUsgClCd;
        private Integer totaTrsAmt;
        private Integer trsClCd;
        private String spstCnfrClNm;
        private String mrntTxprDscmNoEncCntn;
        private String aprvNo;
        private String trsClNm;
        private String spstCnfrClCd;
        private String rcprTin;
        private Integer trsTime;
        private Integer spstCnfrPartNo;
        private Long cshptMrntTin;
        private String trsDtTime;
        private Integer id;
        private Integer trsDt;
        private String incDdcYn;

        public ScrapDataDto(HashMap<String, Object> o) {

            this.rcprTin = o.get("rcprTin").toString();
            this.incDdcNm = o.get("incDdcNm").toString();
            this.mdxpsDdcYn = o.get("mdxpsDdcYn").toString();
            this.statusValue = o.get("statusValue").toString();
            this.ddcYn = o.get("ddcYn").toString();
            this.cshptTrsTypeCd = Integer.parseInt(o.get("cshptTrsTypeCd").toString());
            this.mrntTxprNm = o.get("mrntTxprNm").toString();
            this.cshptTrsTypeNm = o.get("cshptTrsTypeNm").toString();
            this.cshptUsgClCd = Integer.parseInt(o.get("cshptUsgClCd").toString());
            this.totaTrsAmt = Integer.parseInt(o.get("totaTrsAmt").toString());
            this.trsClCd = Integer.parseInt(o.get("trsClCd").toString()) ;
            this.spstCnfrClNm = o.get("spstCnfrClNm").toString();
            this.mrntTxprDscmNoEncCntn = o.get("mrntTxprDscmNoEncCntn").toString();
            this.aprvNo = o.get("aprvNo").toString();
            this.trsClNm = o.get("trsClNm").toString();
            this.spstCnfrClCd = o.get("spstCnfrClCd").toString();
            this.trsTime = Integer.parseInt(o.get("trsTime").toString());
            this.spstCnfrPartNo = Integer.parseInt(o.get("spstCnfrPartNo").toString());
            this.cshptMrntTin = Long.valueOf(o.get("cshptMrntTin").toString());
            this.trsDtTime = o.get("trsDtTime").toString();
            this.id = Integer.parseInt(o.get("id").toString());
            this.trsDt = Integer.parseInt(o.get("trsDt").toString());
            this.incDdcYn = o.get("incDdcYn").toString();
        }


    }


}
