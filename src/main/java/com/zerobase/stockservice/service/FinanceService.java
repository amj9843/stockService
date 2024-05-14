package com.zerobase.stockservice.service;

import com.zerobase.stockservice.domain.Company;
import com.zerobase.stockservice.domain.Dividend;
import com.zerobase.stockservice.dto.CompanyDto;
import com.zerobase.stockservice.dto.DividendDto;
import com.zerobase.stockservice.dto.ScrapedResult;
import com.zerobase.stockservice.dto.constants.CacheKey;
import com.zerobase.stockservice.exception.impl.NoCompanyException;
import com.zerobase.stockservice.repository.CompanyRepository;
import com.zerobase.stockservice.repository.DividendRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class FinanceService {
    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    @Cacheable(key = "#companyName", value = CacheKey.KEY_FINANCE)
    public ScrapedResult getDividendByCompanyName(String companyName) {
        log.info("search company -> " + companyName);

        //1. 회사명을 기준으로 회사 정보를 조회
        Company company = this.companyRepository.findByName(companyName)
                .orElseThrow(() -> new NoCompanyException());

        //2. 조회된 회사 ID로 배당금 정보 조회
        List<Dividend> dividendEntities = this.dividendRepository.findAllByCompanyId(company.getId());

        //3. 결과 조합 후 반환
        List<DividendDto> dividends = new ArrayList<>();
        for (Dividend entity: dividendEntities) {
            dividends.add(new DividendDto(entity.getDate(), entity.getDividend()));
        }

        return new ScrapedResult(new CompanyDto(company.getTicker(), company.getName()), dividends);
    }
}
