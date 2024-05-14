package com.zerobase.stockservice.service;

import com.zerobase.stockservice.domain.Company;
import com.zerobase.stockservice.domain.Dividend;
import com.zerobase.stockservice.dto.DividendDto;
import com.zerobase.stockservice.dto.ScrapedResult;
import com.zerobase.stockservice.exception.impl.NoCompanyException;
import com.zerobase.stockservice.repository.CompanyRepository;
import com.zerobase.stockservice.repository.DividendRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class FinanceServiceTest {
    @InjectMocks
    private FinanceService financeService;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private DividendRepository dividendRepository;

    @DisplayName("기업명으로 배당금 정보 조회")
    @Test
    void getDividendByCompanyName() {
        Long companyId = 1L;
        String ticker = "ticker";
        String companyName = "name";
        LocalDateTime time = LocalDateTime.now();
        String divide = "0.01";

        DividendDto dividendDto = new DividendDto(time, divide);

        //given
        given(companyRepository.findByName(anyString()))
                .willReturn(Optional.of(new Company(companyId, ticker, companyName)));
        given(dividendRepository.findAllByCompanyId(anyLong()))
                .willReturn(List.of(new Dividend(companyId, dividendDto)));

        //when
        ScrapedResult scrapedResult = financeService.getDividendByCompanyName(companyName);

        //then
        assertThat(scrapedResult.getCompanyDto().getName()).isEqualTo(companyName);
        assertThat(scrapedResult.getCompanyDto().getTicker()).isEqualTo(ticker);
        assertThat(scrapedResult.getDividends().size()).isEqualTo(1);
        assertThat(scrapedResult.getDividends().get(0).getDividend()).isEqualTo(divide);
        assertThat(scrapedResult.getDividends().get(0).getDate()).isEqualTo(time);
    }

    @DisplayName("없는 기업명으로 배당금 정보 조회 실패")
    @Test
    void failedGetDividendByCompanyName() {
        Long companyId = 1L;
        String ticker = "ticker";
        String companyName = "name";
        LocalDateTime time = LocalDateTime.now();
        String divide = "0.01";

        //given
        given(companyRepository.findByName(anyString()))
                .willThrow(new NoCompanyException());

        //when
        assertThatThrownBy(() -> financeService.getDividendByCompanyName(companyName))
                .isInstanceOf(NoCompanyException.class);

        //then
    }
}
