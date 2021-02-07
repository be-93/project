package com.backend.project.scrapUtil;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.backend.project.entity.RequestSignData;
import com.backend.project.exception.HomeTaxException;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Component
public class HomeTaxLogin {

    public HashMap<String, String> login(HashMap<String, String> signData) throws Exception{

        log.info("[ ************* homeTaxLogin param *************** ]");
        log.info("cert : " + signData.get("cert"));
        log.info("logSgnt : " + signData.get("logSgnt"));
        log.info("randomEnc : " + signData.get("randomEnc"));
        log.info("pkcEncSsn : " + signData.get("pkcEncSsn"));
        log.info("TXPPsessionID : " + signData.get("TXPPsessionID"));
        log.info("WMONID : " + signData.get("WMONID"));
        log.info("NTS_LOGIN_SYSTEM_CODE_P : " + signData.get("NTS_LOGIN_SYSTEM_CODE_P"));
        log.info("[ ************************************************ ]");

        HashMap<String, String> cookies = new HashMap<String, String>();
        cookies.put("TXPPsessionID",signData.get("TXPPsessionID"));
        cookies.put("WMONID",signData.get("WMONID"));
        cookies.put("NTS_LOGIN_SYSTEM_CODE_P",signData.get("NTS_LOGIN_SYSTEM_CODE_P"));

        Response res = Jsoup.connect("https://hometax.go.kr/pubcLogin.do?domain=hometax.go.kr&mainSys=Y")
                .data(signData)
                .cookies(cookies)
                .timeout(6000)
                .method(Method.POST)
                .execute();

        log.info("국세청 로그인 시도 결과 : " + res.body() );
        log.info("국세청 로그인 COOKIE  : " + res.cookies() );

        // 공인인증서를 통해 로그인 성공시 TXPPsessionID 값이 새로 발급 된다.
        cookies.put("TXPPsessionID",res.cookies().get("TXPPsessionID"));

        boolean login = loginFilter(res.body().toString());

        if(login){
            return cookies;
        }else{
            throw new HomeTaxException("로그인 연동에 실패하였습니다.");
        }

    }


    public boolean loginFilter(String resBody) throws Exception{

        resBody = resBody.replace("'errMsg' : decodeURIComponent('","#");
        resBody = resBody.replace("').replace(/\\+/g,' ').replace(/\\\\n/g,'\\n'), 'lgnRsltCd'","#");

        log.info("loginFilter = " + resBody);
        String result = resBody.substring(resBody.indexOf("#") + 1 , resBody.lastIndexOf("#"));

        if(result.length() > 0){
            throw new HomeTaxException(result);
        }else{
            return true;
        }

    }

    public List<HashMap<String, Object>> scrap(HashMap<String, String> cookies , RequestSignData requestData ) throws Exception {

        /* 헤더 정보 */
        HashMap<String , String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/xml; charset=UTF-8");

        /* 쿠키 정보 */
        cookies.put("NTS_LOGIN_SYSTEM_CODE_P", "TXPP" );
        cookies.put("NTS_REQUEST_SYSTEM_CODE_P", "TXPP" );
        cookies.put("nts_homtax:zoomVal", "100");
        cookies.put("nts_hometax:pkckeyboard", "none");
        cookies.put("nts_hometax:userId", "");
        cookies.put("NetFunnel_ID", "");

        Response res = null;

        res = Jsoup.connect("https://www.hometax.go.kr/permission.do?screenId=index_pp")
                .cookies(cookies)
                .requestBody("<map id='postParam'><popupYn>false</popupYn></map>")
                .timeout(6000)
                .method(Method.POST)
                .execute();

        Document document = DocumentHelper.parseText(res.body());
        // 나중에 데이터를 조회할때 필요한 값
        cookies.put("tin", document.valueOf("//map/tin"));

        String sessionUrl = "";

        if(requestData.getScrapType().equals("TEET")) {
            // 전자세금계산서
            sessionUrl = "https://teet.hometax.go.kr/permission.do?screenId=UTEETBDA01";
        }else if(requestData.getScrapType().equals("TECR")) {
            // 현금영수증
            sessionUrl = "https://tecr.hometax.go.kr/permission.do?screenId=UTECRCB001";
        }

        // 임시 SESSION ID 를 가져옴
        res = Jsoup.connect(sessionUrl)
                .headers(headers)
                .cookies(cookies)
                .requestBody("<map id='postParam'><popupYn>false</popupYn></map>")
                .timeout(6000)
                .method(Method.POST)
                .execute();

//        log.info("[ ************* 임시 SessionID GET 결과  ************* ]");
//        log.info("sessionID  : " + res.body());
//        log.info("sessionID COOKIES INFO : " + res.cookies());
//        log.info("[ ************************************************ ]");

        if(requestData.getScrapType().equals("TEET")) {
            // 전자세금계산서
            cookies.put("TEETsessionID", res.cookies().get("TEETsessionID"));
        }else if(requestData.getScrapType().equals("TECR")) {
            // 현금영수증
            cookies.put("TECRsessionID", res.cookies().get("TECRsessionID"));
        }

        // SESSION ID 를 얻어 SSO TOKEN 을 가져 옴
        String ssoToken = ssoTokenGet(cookies);

        String requestBodyStr = "<map id='postParam'>" + ssoToken + "<popupYn>false</popupYn></map>";

        res = Jsoup.connect(sessionUrl + "&domain=hometax.go.kr")
                .headers(headers)
                .cookies(cookies)
                .requestBody(requestBodyStr)
                .timeout(6000)
                .method(Method.POST)
                .execute();

//        log.info("[ ************* SessionID GET 결과 ************* ]");
//        log.info( res.body() );
//        log.info( "COOKIE INFO" + res.cookies() );
//        log.info("[ ****************************************** ]");

        // SSO TOKEN 을 이용하여 받은 SESSION ID 를 활용해야함
        // 정상 SESSION ID 를 가져오면 시스템 코드는 각 스크래핑 종류에 맞게 세팅해야함
        // 조회 조건 XML 형식의 문자열도 가져옴
        String requestBody = "";
        String scrapUrl = "";
        if(requestData.getScrapType().equals("TEET")) {
            // 전자세금계산서
            cookies.put("TEETsessionID", res.cookies().get("TEETsessionID"));
            cookies.put("NTS_REQUEST_SYSTEM_CODE_P","TEET");
            requestBody = searchXmlGet(cookies, requestData);
            // 스크랩핑해올 URL 정보도 세팅해둔다.
            scrapUrl = "https://teet.hometax.go.kr/wqAction.do?actionId=ATEETBDA001R01&screenId=UTEETBDA01&popupYn=false&realScreenId=";
        }else if(requestData.getScrapType().equals("TECR")) {
            // 현금영수증
            cookies.put("TECRsessionID", res.cookies().get("TECRsessionID"));
            cookies.put("NTS_REQUEST_SYSTEM_CODE_P","TECR");
            requestBody = searchXmlGet(cookies, requestData);
            // 스크랩핑해올 URL 정보도 세팅해둔다.
            scrapUrl = "https://tecr.hometax.go.kr/wqAction.do?actionId=ATECRCBA001R01&screenId=UTECRCB001&popupYn=false&realScreenId=";
        }

        String netFunnelId = netFunnelIdGet(cookies);
        cookies.put("NetFunnel_ID",netFunnelId);

        res = Jsoup.connect(scrapUrl)
                .cookies(cookies)
                .requestBody(requestBody)
                .headers(headers)
                .timeout(6000)
                .method(Method.POST)
                .ignoreContentType(true) // 컨텐츠 타입을 무시하고 가져오도록
                .execute();

        log.info("[ ************* 드래곤볼 GET 결과 ************* ]");
        log.info( res.body() );
        log.info( "COOKIE INFO" + res.cookies() );
        log.info("[ ****************************************** ]");

        // XML 로 받아온 데이터를 JSON 형식으로 변환
        org.json.JSONObject xmlJsonObject = org.json.XML.toJSONObject(res.body());

        xmlJsonObject = (org.json.JSONObject) xmlJsonObject.get("map");
        xmlJsonObject = (org.json.JSONObject) xmlJsonObject.get("list");

        String xmlJsonString = null;
        if(!xmlJsonObject.isNull("map")) {
            xmlJsonString = xmlJsonObject.get("map").toString();
        }

        org.json.JSONArray jsonArray = new org.json.JSONArray(xmlJsonString);
        List<HashMap<String, Object>> result = new ArrayList<>();
        for (Object o : jsonArray) {
            result.add(new ObjectMapper().readValue(o.toString(), new TypeReference<HashMap<String, Object>>(){}));
        }
        return result;

    }


    /*
     * 조회XML 양식 만들어주는 Method
     */
    public String searchXmlGet(HashMap<String, String> cookies , RequestSignData requestData) throws Exception{

        String requestBody = "";

        if(requestData.getScrapType().equals("TEET")) {
            requestBody = "<map id=\"ATEETBDA001R01\">"
                    + " <icldLsatInfr>N</icldLsatInfr>"
                    + " <resnoSecYn>Y</resnoSecYn>"
                    + " <srtClCd>1</srtClCd>"
                    + " <srtOpt>01</srtOpt>"
                    + " <map id=\"pageInfoVO\">"
                    + " <pageSize>50</pageSize>" // 최대 50개까지 출력할수있음
                    + " <pageNum>1</pageNum>"
                    + " </map>"
                    + " <map id=\"excelPageInfoVO\" />"
                    + " <map id=\"etxivIsnBrkdTermDVOPrmt\">"
                    + " <tnmNm />"
                    + " <prhSlsClCd>02</prhSlsClCd>" // [01] 매출 / [02] 매입
                    + " <dtCl>01</dtCl>"
                    + " <bmanCd>01</bmanCd>"
                    + " <etxivClsfCd>all</etxivClsfCd>"
                    + " <isnTypeCd>all</isnTypeCd>"
                    + " <pageSize>10</pageSize>"
                    + " <splrTin></splrTin>" // 공급자
                    + " <dmnrTin>" + cookies.get("tin") + "</dmnrTin>" // 공급받는자
                    + " <cstnBmanTin></cstnBmanTin>"
                    + " <splrTxprDscmNo></splrTxprDscmNo>"
                    + " <dmnrTxprDscmNo></dmnrTxprDscmNo>"
                    + " <splrMpbNo></splrMpbNo>"
                    + " <dmnrMpbNo></dmnrMpbNo>"
                    + " <cstnBmanMpbNo></cstnBmanMpbNo>"
                    + " <etxivClCd>01</etxivClCd>"
                    + " <etxivKndCd>all</etxivKndCd>"
                    + " <splrTnmNm></splrTnmNm>"
                    + " <dmnrTnmNm></dmnrTnmNm>"
                    + " <inqrDtStrt>" + requestData.getStart_dt() + "</inqrDtStrt>"
                    + " <inqrDtEnd>" + requestData.getEnd_dt() + "</inqrDtEnd> "
                    + " </map>"
                    + " </map>";
        }else if(requestData.getScrapType().equals("TECR")) {
            requestBody = "<map id=\"ATECRCBA001R01\">\n"
                    +"<trsDtRngStrt>" + requestData.getStart_dt() + "</trsDtRngStrt>\n"
                    +"<trsDtRngEnd>" + requestData.getEnd_dt() + "</trsDtRngEnd>\n"
                    +"<spjbTrsYn/><cshptUsgClCd/>\n"
                    +"<sumTotaTrsAmt/>\n"
                    +"<tin>" + cookies.get("tin") + "</tin>\n"
                    +"<totalCount>0</totalCount>\n"
                    +"<sumSplCft>22818</sumSplCft>\n"
                    +"<map id=\"pageInfoVO\">\n"
                    +"<pageSize>10</pageSize>\n"
                    +"<pageNum>1</pageNum>\n"
                    +"<totalCount>1</totalCount>\n"
                    +"</map>\n"
                    + "</map>" ;
        }

        return requestBody;
    }


    /*
     * netFunnelId 값을 가져오는 Method
     */
    public String netFunnelIdGet(HashMap<String, String> cookies) throws Exception{

        HashMap<String , String> data = new HashMap<>();

        data.put("opcode","5101");
        data.put("nfid","0");
        data.put("prefix","NetFunnel.gRtype=5101");
        data.put("sid","service_2");
        data.put("aid","UTEETBDA01");
        data.put("js","yes");

        Response res = Jsoup.connect("https://apct.hometax.go.kr/ts.wseq")
                .data(data)
                .cookies(cookies)
                .timeout(6000)
                .method(Method.GET)
                .ignoreContentType(true) // 컨텐츠 타입을 무시하고 가져오도록
                .execute();

//        log.info("[ ************* NetFunnel GET 결과 ************* ]");
//        log.info( res.body() );
//        log.info( "COOKIE INFO" + res.cookies() );
//        log.info("[ ****************************************** ]");

        String netFunnelDecoder = res.body();

        // NetFunnel_ID값만 얻기 위해 공백으로 치환
        String netFunnel = netFunnelDecoder.replace("NetFunnel.gRtype=5101NetFunnel.gControl.result='", "").replace("'; NetFunnel.gControl._showResult();", "");

        return netFunnel;
    }

    /*
     * SSO TOKEN URL 주소 만들어주기
     * */
    public String ssoTokenGet(HashMap<String, String> cookies) throws Exception{

        String today = new SimpleDateFormat("yyyy_MM_dd").format(new Date());
        String seed = "qwertyuiopasdfghjklzxxcvbnm0123456789QWERTYUIOPASDDFGHJKLZXCVBNBM";
        String randomString = "";
        for (int i = 0; i < 20; i++) {
            Double d = Math.floor(Math.random() * seed.length());
            randomString += seed.charAt(d.intValue());
        }

        String ssoTokenUrl = "https://hometax.go.kr/token.do?query=" + "_" + randomString + "&postfix=" + today;

//        log.info("[ ************* token.do param ************* ]");
//        log.info("SSO TOKEN URL : " + ssoTokenUrl );
//        log.info("WMONID : " + cookies.get("WMONID"));
//        log.info("TXPPsessionID : " + cookies.get("TXPPsessionID"));
//        log.info("TEETsessionID : " + cookies.get("TEETsessionID"));
//        log.info("NetFunnel_ID : " + cookies.get("NetFunnel_ID"));
//        log.info("NTS_REQUEST_SYSTEM_CODE_P : " + cookies.get("NTS_REQUEST_SYSTEM_CODE_P"));
//        log.info("NTS_LOGIN_SYSTEM_CODE_P : " + cookies.get("NTS_LOGIN_SYSTEM_CODE_P"));
//        log.info("nts_homtax:zoomVal : " + cookies.get("nts_homtax:zoomVal"));
//        log.info("nts_hometax:pkckeyboard : " + cookies.get("nts_hometax:pkckeyboard"));
//        log.info("[ ****************************************** ]");

        Response res = Jsoup.connect(ssoTokenUrl)
                .cookies(cookies)
                .timeout(6000)
                .method(Method.GET)
                .execute();

        String ssoTokenDecoder = res.body();

        // 콜백 함수명 지우고 토큰값만 얻기 위해 공백으로 치환
        String ssoToken = ssoTokenDecoder.replace("nts_reqPortalCallback(\"", "").replace("\");", "");

        return ssoToken;
    }

}
