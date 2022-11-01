# :newspaper: HOMETAX SCRAP
HOMETAX SCRAP 는 공인인증서를 이용하여 홈택스 로그인과 세금계산서 / 현금영수증 조회 스크래핑을 할 수 있으며,   
REST API 를 통하여 타 시스템과의 연계 할 수 있도록 REST API 를 지원합니다.:grin:

# :hammer: PROJECT STACK
    Spring boot 2.4.2 / Spring Data JPA / Gradle / Lombok / h2 DataBase
# Dependencies List
    compile group: 'org.bouncycastle', name: 'bcprov-jdk16', version: '1.46'
	compile group: 'org.jsoup', name: 'jsoup', version: '1.13.1'
	compile group: 'org.slf4j:slf4j-api:1.7.5'
	compile group: 'com.googlecode.json-simple', name: 'json-simple', version: '1.1.1'
	compile group: 'dom4j', name: 'dom4j', version: '1.6.1'
	compile group: 'commons-io', name: 'commons-io', version: '2.8.0'
	compile group: 'org.apache.commons', name: 'commons-lang3', version: '3.11'
	compile group: 'bouncycastle', name: 'bcprov-jdk15', version: '140'
	compile group: 'jaxen', name: 'jaxen', version: '1.2.0'
	compile group: 'org.json', name: 'json', version: '20201115'

# :star: 전반적인 로직 설명
- 1.공인증서 복호화 하여 필요 값 추출
  - 공인인증서 Private Key 추출 
  - 공인인증서 Random Key 추출
- 2.공인인증서 전자서명
  - 전자서명 진행시 홈택스에서 서명에 사용될 문자열과 쿠키값 정보 필요
- 3.홈택스 로그인 및 스크래핑


# :pushpin:
- Source Repository : (https://github.com/be-93/project)
- 공인인증서 관련
    - [공인인증서 복호화](../md/공인인증서_복호화.md)
    - [공인인증서 전자서명](../md/공인인증서_전자서명.md)
    - [홈택스 로그인 연동](../md/홈택스_로그인.md)
- [REST API 를 이용한 홈택스 데이터 수집](../md/API_V1.md)
- [JPA 를 사용해 간단하게 홈택스 데이터 저장](../md/API_V2.md)
- [ExceptionHandler 를 이용한 홈택스 예외 처리](../md/홈택스_예외처리.md)

