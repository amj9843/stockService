package com.zerobase.stockservice.scheduler;

import com.zerobase.stockservice.domain.Company;
import com.zerobase.stockservice.domain.Dividend;
import com.zerobase.stockservice.dto.CompanyDto;
import com.zerobase.stockservice.dto.DividendDto;
import com.zerobase.stockservice.dto.ScrapedResult;
import com.zerobase.stockservice.repository.CompanyRepository;
import com.zerobase.stockservice.repository.DividendRepository;
import com.zerobase.stockservice.schduler.ScraperSchduler;
import com.zerobase.stockservice.scraper.Scraper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ScraperSchedulerTest {
    @InjectMocks
    private ScraperSchduler scraperScheduler;
    @Mock
    private Scraper scraper;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private DividendRepository dividendRepository;
    @Captor
    private ArgumentCaptor<List<Dividend>> dividendCaptor;

    @DisplayName("특정 시간마다 DB에 저장된 기업의 배당금 스크래핑")
    @Test
    void yahooFinanceSchedule() {
        Long companyId = 1L;
        String name = "MMM";
        String ticker = "3M Company";
        LocalDateTime time = LocalDateTime.now();
        String divide = "0.04";

        CompanyDto companyDto = new CompanyDto(ticker, name);
        DividendDto dividendDto = new DividendDto(time, divide);

        //given
        given(companyRepository.findAll())
                .willReturn(List.of(new Company(companyId, ticker, name)));
        given(scraper.scrap(any()))
                .willReturn(new ScrapedResult(companyDto, List.of(dividendDto)));
        given(dividendRepository.existsByCompanyIdAndDate(anyLong(), any()))
                .willReturn(false);
        given(dividendRepository.save(any()))
                .willReturn(new Dividend(companyId, dividendDto));

        //when
        scraperScheduler.yahooFinanceScheduling();
        ArgumentCaptor<Dividend> dividendCaptor = ArgumentCaptor.forClass(Dividend.class);

        //then
        verify(dividendRepository, times(1)).save(dividendCaptor.capture());
        assertThat(dividendCaptor.getValue().getDividend()).isEqualTo(divide);
        assertThat(dividendCaptor.getValue().getDate()).isEqualTo(time);
    }
}
