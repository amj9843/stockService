package com.zerobase.stockservice.scraper;

import com.zerobase.stockservice.dto.CompanyDto;
import com.zerobase.stockservice.dto.ScrapedResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class YahooFinanceScraperTest {
    @InjectMocks
    private YahooFinanceScraper yahooFinanceScraper;

    @DisplayName("특정 기업의 배당금 정보 스크래핑")
    @Test
    void scrap() {
        //given
        String ticker = "MMM";
        String name = "3M Company";

        CompanyDto companyDto = new CompanyDto(ticker,name);

        //when
        ScrapedResult scrapedResult = yahooFinanceScraper.scrap(companyDto);

        //then
        assertThat(scrapedResult.getCompanyDto().getTicker()).isEqualTo(ticker);
        assertThat(scrapedResult.getCompanyDto().getName()).isEqualTo(name);
        assertNotNull(scrapedResult.getDividends());
    }

    @DisplayName("ticker를 통해 기업 정보 스크래핑")
    @Test
    void scrapCompanyByTicker() {
        //given
        String ticker = "MMM";

        //when
        CompanyDto companyDto = yahooFinanceScraper.scrapCompanyByTicker(ticker);

        //then
        assertThat(companyDto.getTicker()).isEqualTo(ticker);
        assertThat(companyDto.getName()).isEqualTo("3M Company");
    }
}
