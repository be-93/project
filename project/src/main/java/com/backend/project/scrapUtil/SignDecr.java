package com.backend.project.scrapUtil;

import com.backend.project.entity.RequestSignData;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.pkcs.EncryptedPrivateKeyInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.crypto.PBEParametersGenerator;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SignDecr {

    public PrivateKey getPrivateKey(byte[] decryptedKey) throws Exception {
        // 복호화된 내용을 PrivateKey 객체로 변환
        PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(decryptedKey);
        KeyFactory kf = KeyFactory.getInstance("RSA", "BC");
        return kf.generatePrivate(ks);
    }

    public byte[] getDecryptedKey(RequestSignData requestSignData) throws Exception {

        byte[] decryptedKey = null;
        byte[] encodedKey = requestSignData.getSingPri().getBytes();

        try {
            ByteArrayInputStream bIn = new ByteArrayInputStream(encodedKey);
            ASN1InputStream aIn = new ASN1InputStream(bIn);

            ASN1Sequence asn1Sequence = (ASN1Sequence) aIn.readObject();

            // asn1Sequence 0번째 인덱스에는 알고리즘 식별자와 키의 길이가 들어가있음
            // https://docs.microsoft.com/ko-kr/dotnet/api/system.security.cryptography.pkcs.algorithmidentifier?view=dotnet-plat-ext-5.0 참고 레퍼런스
            AlgorithmIdentifier algId = AlgorithmIdentifier.getInstance(asn1Sequence.getObjectAt(0));

            // asn1Sequence 1번째 암호화 된 데이터
            ASN1OctetString data = ASN1OctetString.getInstance(asn1Sequence.getObjectAt(1));

            // EncryptedPrivateKeyInfo 클래스 관한 레퍼런스
            // http://cris.joongbu.ac.kr/course/java/api/javax/crypto/EncryptedPrivateKeyInfo.html
            // 암호화에 사용된 알고리즘 과 암호화된 데이터를 사용하라고 명시되어있음
            EncryptedPrivateKeyInfo encryptedPrivateKeyInfo = new EncryptedPrivateKeyInfo(algId, data.getEncoded());

            // 알고리즘 명칭을 추출해냄
            String privateKeyAlgName = encryptedPrivateKeyInfo.getEncryptionAlgorithm().getAlgorithm().getId();

            // addProvider를 하지 않으면 복호화 과정중에 오류가 나는 알고리즘 케이스가 존재하여 추가하였음.
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

            if ("1.2.840.113549.1.5.13".equals(privateKeyAlgName)) {
                // PKCS5PBES2 방식의 알고리즘 복호화 IN

                // PKCS5PBES2방식의 알고리즘은 seedCBCWithSHA1 와 다르게 개인키 복호화에 대한 정보가
                // 모두 쉽게 추출할 수 있어 seedCBCWithSHA1 보다 간단하다.

                // 개인키 암호화 정보에서 Salt, Iteration Count(IC), Initial Vector(IV)를 추출한다.
                ASN1Sequence asn1Sequence2 = (ASN1Sequence)algId.getParameters();
                ASN1Sequence asn1Sequence3 = (ASN1Sequence)asn1Sequence2.getObjectAt(0);

                // PBKDF2 Key derivation algorithm
                ASN1Sequence asn1Sequence33 = (ASN1Sequence)asn1Sequence3.getObjectAt(1);

                // Salt 값
                DEROctetString derOctetStringSalt = (DEROctetString)asn1Sequence33.getObjectAt(0);

                // Iteration Count(IC)
                ASN1Integer asn1IntegerIC = (ASN1Integer)asn1Sequence33.getObjectAt(1);
                ASN1Sequence asn1Sequence4 = (ASN1Sequence)asn1Sequence2.getObjectAt(1);

                // Initial Vector(IV)
                DEROctetString derOctetStringIV = (DEROctetString)asn1Sequence4.getObjectAt(1);

                // 복호화 키 생성
                int keySize = 256;
                PBEParametersGenerator generator = new PKCS5S2ParametersGenerator();
                generator.init( PBEParametersGenerator.PKCS5PasswordToBytes(requestSignData.getSignPassword().toCharArray()), derOctetStringSalt.getOctets(), asn1IntegerIC.getValue().intValue());
                byte[] iv = derOctetStringIV.getOctets(); KeyParameter key = (KeyParameter)generator.generateDerivedParameters(keySize);

                // 복호화 수행
                IvParameterSpec ivSpec = new IvParameterSpec(iv);

                // 지정된 바이트 배열을 이용하여 비밀키를 구현
                // 참고 레퍼런스 : http://cris.joongbu.ac.kr/course/java/api/javax/crypto/spec/SecretKeySpec.html
                // 비밀키의 키 데이터와 알고리즘 명칭을 paramter 로 대입한다.
                SecretKeySpec secKey = new SecretKeySpec(key.getKey(), "SEED");

                // 암호화 방식이 패딩방식일 경우 SEED/CBC/PKCS5Padding 값은 고유의 기본값으로 설정해야함.
                // 참고 레퍼런스 : http://cris.joongbu.ac.kr/course/java/api/
                Cipher cipher = Cipher.getInstance("SEED/CBC/PKCS5Padding", "BC");

                // Cipher.DECRYPT_MODE 복호화 모드에 사용됨
                cipher.init(Cipher.DECRYPT_MODE, secKey, ivSpec);
                decryptedKey = cipher.doFinal(data.getOctets());
            } else {
                // 1.2.410.200004.1.15 알고리즘 규칙
                // seedCBCWithSHA1 방식의 알고리즘 복호화 IN

                // PKCS5PBES2 방식과 다르게 seedCBCWithSHA1 방식의 규칙은 복호화 키와 IV값을 얻어내기 위해 아래 레퍼런스를 참고해야함.
                // https://rootca.kisa.or.kr/kcac/down/TechSpec/2.3-Encryption%20Algorithm%20Scheme%20Specification.pdf

                ASN1Sequence asn1Sequence2 = (ASN1Sequence)algId.getParameters();
                // Salt 값
                DEROctetString derOctetStringSalt = (DEROctetString)asn1Sequence2.getObjectAt(0);
                // Iteration Count(IC)
                ASN1Integer asn1IntegerIC = (ASN1Integer)asn1Sequence2.getObjectAt(1);

                // 복호화 키 생성
                // seedCBCWithSHA1 방식은 Salt값과 IC값을 추출하여
                // PBKDF1에 패스워드와 , Salt , IC값을 적용하여 20바이트의 추출키를 생성해야한다.

                // 20바이트 변수 생성
                byte[] dk = new byte[20];
                MessageDigest md = MessageDigest.getInstance("SHA1");

                // 패스워드 삽입
                md.update(requestSignData.getSignPassword().getBytes());

                // Salt 값 삽입
                md.update(derOctetStringSalt.getOctets());
                dk = md.digest();
                for (int i = 1; i < asn1IntegerIC.getValue().intValue(); i++) {
                    dk = md.digest(dk);
                }
                byte[] keyData = new byte[16];
                System.arraycopy(dk, 0, keyData, 0, 16);

                byte[] digestBytes = new byte[4];
                System.arraycopy(dk, 16, digestBytes, 0, 4);

                MessageDigest digest = MessageDigest.getInstance("SHA-1");
                digest.reset();
                digest.update(digestBytes);

                byte[] div = digest.digest();

                // Initial Vector(IV) 생성
                byte[] iv = new byte[16];
                System.arraycopy(div, 0, iv, 0, 16);

                // IV = "123456789012345" 첫 번째 로 고정하여 초기 벡터를 사용할 때
                if ("1.2.410.200004.1.4".equals(privateKeyAlgName)) {
                    iv = "012345678912345".getBytes();
                }

                // 복호화 수행
                IvParameterSpec ivSpec = new IvParameterSpec(iv);

                // 지정된 바이트 배열을 이용하여 비밀키를 구현
                // 참고 레퍼런스 : http://cris.joongbu.ac.kr/course/java/api/javax/crypto/spec/SecretKeySpec.html
                // 비밀키의 키 데이터와 알고리즘 명칭을 paramter 로 대입한다.
                SecretKeySpec secKey = new SecretKeySpec(keyData, "SEED");

                // 암호화 방식이 패딩방식일 경우 SEED/CBC/PKCS5Padding 값은 고유의 기본값으로 설정해야함.
                // 참고 레퍼런스 : http://cris.joongbu.ac.kr/course/java/api/
                Cipher cipher = Cipher.getInstance("SEED/CBC/PKCS5Padding", "BC");
                // Cipher.DECRYPT_MODE 복호화 모드에 사용됨
                cipher.init(Cipher.DECRYPT_MODE, secKey, ivSpec);
                decryptedKey = cipher.doFinal(data.getOctets());
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return decryptedKey;

    }

    /*
     * 공인인증서 개인키에서 신원확인 값 추출 Method
     * Random Value
     */
    public String getIdentityCheck(RequestSignData requestSignData) throws Exception{

        if (null == requestSignData.getSignPassword() || "".equals(requestSignData.getSignPassword())) {
            return null;
        }
        try {
            byte[] decryptedKey = getDecryptedKey(requestSignData);
            try (ByteArrayInputStream bIn2 = new ByteArrayInputStream(decryptedKey); ASN1InputStream aIn2 = new ASN1InputStream(bIn2);) {

                ASN1Object asn1Object = (ASN1Object) aIn2.readObject();
                DERSequence seq = (DERSequence) asn1Object.toASN1Object();
                //log.info("DLSequence seq size : " +  seq.size());

                int i = 0;
                while (i < seq.size()) {
                    //log.info("CLASS NAME : " +  seq.getObjectAt(i).getClass().getName());
                    if (seq.getObjectAt(i) instanceof DERTaggedObject) {
                        DERTaggedObject dertTaggedObject = (DERTaggedObject) seq.getObjectAt(i);
                        if (dertTaggedObject.getTagNo() == 0) {
                            DERSequence seq2 = (DERSequence) dertTaggedObject.getObject();
                            //log.info("seq2 : " +  seq2.toString());
                            int j = 0;
                            while (j < seq2.size()) {
                                //log.info("seq2.getObjectAt(i)" +  seq2.getObjectAt(j).getClass().getName());
                                if (seq2.getObjectAt(j) instanceof ASN1ObjectIdentifier) {
                                    ASN1ObjectIdentifier idRandomNumOID = (ASN1ObjectIdentifier) seq2.getObjectAt(j);
                                    //log.info("idRandomNumOID : " +  idRandomNumOID.toString());
                                    if ("1.2.410.200004.10.1.1.3".equals(idRandomNumOID.toString())) {
                                        DERSet derSet = (DERSet) seq2.getObjectAt(j + 1);
                                        DERBitString DERBitString = (DERBitString) derSet.getObjectAt(0);
                                        //log.info("DERBitString : " +  DERBitString);
                                        DEROctetString DEROctetString = new DEROctetString(DERBitString.getBytes());
                                        return Base64.getEncoder().encodeToString(DEROctetString.getOctets());
                                    }
                                }
                                j++;
                            }
                        }
                    }
                    i++;
                }
            }
        } catch (Exception e) {
            log.error("getIdentityCheck 오류 : ", e);
        }
        return null;
    }

    public X509Certificate getCertificate(RequestSignData requestSignData) throws Exception {
        X509Certificate X509certificate = null;
        try {
            X509certificate = (X509Certificate) CertificateFactory.getInstance("X509").generateCertificate(requestSignData.getSingCert().getInputStream());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return X509certificate;
    }

    /**
     * 전자서명 Method
     * @return byte[]
     * @throws Exception
     * @param requestSignData
     */
    public HashMap<String, String> sign(RequestSignData requestSignData) throws Exception {

        byte[] privateKeyByte = getDecryptedKey(requestSignData);
        X509Certificate certificate = getCertificate(requestSignData);
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String toDay = sdf.format(date);

        // 개인키
        PrivateKey privateKey = getPrivateKey(privateKeyByte);

        // 공개키
        PublicKey publicKey = certificate.getPublicKey();

        // 개인키 신원확인 키 값
        String privateRandomValue = getIdentityCheck(requestSignData);

        // 서명용 문자열과 쿠키 정보들을 가져옴
        JSONObject signTextInfo = getSignText();

        String msg = signTextInfo.get("pkcEncSsn").toString();

        Signature signaturePrivate = Signature.getInstance("SHA256withRSA");//SHA256withRSA

        signaturePrivate.initSign(privateKey);

        signaturePrivate.update(msg.getBytes());

        byte[] sign = signaturePrivate.sign();

        String msgB = msg;

        Signature signaturePublic = Signature.getInstance("SHA256withRSA");

        signaturePublic.initVerify(publicKey);

        signaturePublic.update(msgB.getBytes());

        boolean verifty = signaturePublic.verify(sign);

        log.info("전자서명 검증 결과 : " + verifty);

        log.info("[ ******************************************************************* ]");
        log.info("서명용 공개키 일렬번호 : " + certificate.getSerialNumber());
        log.info("전자서명한 값 : " +  Base64.getEncoder().encodeToString(sign) );
        log.info("서명용 공개키 BASE 64 PEM : " + "-----BEGIN CERTIFICATE-----" + Base64.getEncoder().encodeToString(certificate.getEncoded()) + "-----END CERTIFICATE-----" );
        log.info("서명용 개인키 RANDOM 값 : " + privateRandomValue);
        log.info("[ ******************************************************************* ]");

        String certPem = "-----BEGIN CERTIFICATE-----" + Base64.getEncoder().encodeToString(certificate.getEncoded()) + "-----END CERTIFICATE-----";
        String logSgnt = signTextInfo.get("pkcEncSsn").toString() + "$" + certificate.getSerialNumber() + "$" + toDay + "$" + Base64.getEncoder().encodeToString(sign) ;
        logSgnt = Base64.getEncoder().encodeToString( logSgnt.getBytes() );
        HashMap<String, String> param = new HashMap<String, String>();


        param.put("cert", certPem);
        param.put("logSgnt", logSgnt); //서명으로 사용할 문자열 + $ + 서명용공개키 인증서 일렬번호 + $ + yyyyMMddHHmmss + $ + 전자서명한 값
        param.put("pkcLgnClCd", "04");
        param.put("pkcLoginYnImpv", "Y");
        param.put("randomEnc",privateRandomValue);
        param.put("pkcEncSsn",signTextInfo.get("pkcEncSsn").toString());
        param.put("WMONID",signTextInfo.get("WMONID").toString());
        param.put("TXPPsessionID",signTextInfo.get("TXPPsessionID").toString());
        param.put("NTS_LOGIN_SYSTEM_CODE_P", "TXPP");

        return param;
    }

    /**
     * 서명용 문자열 가져오는 Method
     * @return
     * @throws Exception
     */
    public JSONObject getSignText() throws Exception {

        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);
        HttpURLConnection conn = null;

        URL url = new URL("https://www.hometax.go.kr/wqAction.do?actionId=ATXPPZXA001R01&screenId=UTXPPABA01");
        conn = (HttpURLConnection) url.openConnection();
        //conn.getContent();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));

        bw.flush();
        bw.close();

        int responseCode = conn.getResponseCode();
        if (responseCode == 400) {
            System.out.println("400 - ERROR");
        } else if (responseCode == 401) {
            System.out.println("401 - ERROR");
        } else if (responseCode == 500) {
            System.out.println("500 - ERROR");
        } else {

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            JSONParser parser = new JSONParser();
            JSONObject result = (JSONObject) parser.parse(sb.toString());

            //log.info("Response 결과 : "+conn.getHeaderFields());

            List<HttpCookie> cookies = cookieManager.getCookieStore().getCookies();
            for (HttpCookie cookie : cookies) {
                result.put(cookie.getName(), cookie.getValue());
            }

            log.info("[ ************* 서명 문자열 GET 결과 ************* ]");
            log.info( "WMONID : " + result.get("WMONID") );
            log.info( "TXPPsessionID : " + result.get("TXPPsessionID") );
            log.info( "pkcEncSsn : " + result.get("pkcEncSsn") );
            log.info("[ ****************************************** ]");

            return result;
        }
        return null;
    }

}
