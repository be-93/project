package com.backend.project.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.HashMap;

@Entity
@RequiredArgsConstructor
@Getter
@Table(name = "scrap_data")
public class Scrap {

    // 승인번호 이므로 Primary Key 로 지정
    @Id
    private String aprvNo;

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

    // 신규 데이터 입력시 사용될 편의 메소드
    public void createScrapData(HashMap<String, Object> o) {

        this.aprvNo = o.get("aprvNo").toString();
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

    // 기존 데이터가 있을경우 업데이트 진행해주는 편의메소드
    public void updateScrapData(HashMap<String, Object> o) {

        // 승인번호는 Primary Key 이므로 업데이트를 해주지 않는게 원칙.
        // this.aprvNo = o.get("aprvNo").toString();

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
