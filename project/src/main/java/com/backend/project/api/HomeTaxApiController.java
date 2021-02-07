package com.backend.project.api;

import com.backend.project.entity.RequestSignData;
import com.backend.project.scrapUtil.HomeTaxLogin;
import com.backend.project.scrapUtil.SignDecr;
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
public class HomeTaxApiController {

    private final SignDecr signDecr;
    private final HomeTaxLogin homeTaxLogin;

    @RequestMapping(value = "/api/v1/scrap" , method = RequestMethod.POST)
    public Result scrap(RequestSignData request) throws Exception {
        HashMap<String, String> sign = signDecr.sign(request);
        HashMap<String, String> login = homeTaxLogin.login(sign);
        List<HashMap<String, Object>> result = homeTaxLogin.scrap(login, request);
        return new Result(result.stream().map(ScrapData::new).collect(Collectors.toList()));
    }

    @Data
    @AllArgsConstructor
    static class Result<T> {
        private T data;
    }

    // 리턴해줄 데이터 셋
    @Data
    public class ScrapData {

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

        public ScrapData(HashMap<String, Object> o) {

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
