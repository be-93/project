package com.backend.project.service;

import com.backend.project.entity.RequestSignData;
import com.backend.project.entity.Scrap;
import com.backend.project.repository.ScrapRepository;
import com.backend.project.scrapUtil.HomeTaxLogin;
import com.backend.project.scrapUtil.SignDecr;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ScrapService {

    private final SignDecr signDecr;
    private final HomeTaxLogin homeTaxLogin;
    private final EntityManager em;
    private final ScrapRepository scrapRepository;

    public List<HashMap<String, Object>> scrapDirectCall(RequestSignData request) throws Exception{

        // 필수값 검증 편의 메소드
        request.hashError();

        HashMap<String, String> sign = signDecr.sign(request);
        HashMap<String, String> login = homeTaxLogin.login(sign);
        List<HashMap<String, Object>> result = homeTaxLogin.scrap(login, request);

        return result;
    }

    @Transactional
    public List<HashMap<String, Object>> scrapV2(RequestSignData request) throws Exception {
        List<HashMap<String, Object>> result = scrapDirectCall(request);
        for (HashMap<String, Object> scrapItem : result) {
            Optional<Scrap> findScrap = scrapRepository.findById(scrapItem.get("aprvNo").toString());
            if (findScrap.isPresent()) {
                // 기존에 해당 데이터가 이미 스크래핑 되어 수집되어 있는 상태라면 값만 업데이트 해준다.
                findScrap.get().updateScrapData(scrapItem);
            }else{
                // 신규 데이터 이므로 데이터를 추가하여줌.
                Scrap newScrap = new Scrap();
                newScrap.createScrapData(scrapItem);
                em.persist(newScrap);
            }
        }
        return result;
    }

}
