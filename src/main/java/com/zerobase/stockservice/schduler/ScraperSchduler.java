package com.zerobase.stockservice.schduler;

import com.zerobase.stockservice.domain.Company;
import com.zerobase.stockservice.domain.Dividend;
import com.zerobase.stockservice.dto.CompanyDto;
import com.zerobase.stockservice.dto.ScrapedResult;
import com.zerobase.stockservice.dto.constants.CacheKey;
import com.zerobase.stockservice.repository.CompanyRepository;
import com.zerobase.stockservice.repository.DividendRepository;
import com.zerobase.stockservice.scraper.Scraper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@EnableCaching
@AllArgsConstructor
public class ScraperSchduler {
    private final Scraper yahooFinanceScraper;
    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;
    
    //일정 주기마다 수행
    @CacheEvict(value = CacheKey.KEY_FINANCE, allEntries = true)
    @Scheduled(cron = "${scheduler.scrap.yahoo}")
    public void yahooFinanceScheduling() {
        //저장된 회사 목록 조회
        List<Company> companies = this.companyRepository.findAll();
        
        //회사마다 배당금 정보를 새로 스크래핑
        for (Company company: companies) {
            log.info("scraping scheduler is started! -> " + company.getName());
            ScrapedResult scrapedResult = this.yahooFinanceScraper.scrap(new CompanyDto(
                    company.getTicker(), company.getName()
            ));

            //스크래핑한 배당금 정보 중 데이터베이스에 없는 것은 저장
            scrapedResult.getDividends().stream()
                            .map(e -> new Dividend(company.getId(), e))
                            .forEach(e -> {
                                boolean exists = this.dividendRepository.existsByCompanyIdAndDate(e.getCompanyId(), e.getDate());
                                if (!exists) {
                                    this.dividendRepository.save(e);
                                }
                            });

            //연속적으로 스크래핑 대상 사이트 서버에 요청을 날리지 않도록 일시정지
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
