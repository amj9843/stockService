package com.zerobase.stockservice.service;

import com.zerobase.stockservice.domain.Company;
import com.zerobase.stockservice.domain.Dividend;
import com.zerobase.stockservice.dto.CompanyDto;
import com.zerobase.stockservice.dto.DividendDto;
import com.zerobase.stockservice.dto.ScrapedResult;
import com.zerobase.stockservice.exception.impl.AlreadyExistTickerException;
import com.zerobase.stockservice.exception.impl.NoCompanyException;
import com.zerobase.stockservice.repository.CompanyRepository;
import com.zerobase.stockservice.repository.DividendRepository;
import com.zerobase.stockservice.scraper.Scraper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CompanyServiceTest {
    @Spy
    @InjectMocks
    private CompanyService companyService;
    @Mock
    private Scraper scraper;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private DividendRepository dividendRepository;
    @Captor
    private ArgumentCaptor<List<Dividend>> dividendCaptor;

    @DisplayName("ticker로 DB에 데이터가 존재하는지 확인," +
            "없는 경우 기업 정보와 배당급 정보를 스크래핑하여 저장")
    @Test
    void save() {
        Long companyId = 1L;
        String name = "companyName";
        String ticker = "ticker";
        String divide = "0.04";

        LocalDateTime time = LocalDateTime.now();

        CompanyDto companyDto = new CompanyDto(ticker, name);
        DividendDto dividendDto = new DividendDto(time, divide);

        //given
        given(companyRepository.existsByTicker(anyString())).willReturn(false);
        given(scraper.scrapCompanyByTicker(anyString())).willReturn(companyDto);
        given(companyRepository.save(any()))
                .willReturn(new Company(companyId, ticker, name));
        given(scraper.scrap(any()))
                .willReturn(new ScrapedResult(companyDto, List.of(dividendDto)));
        given(dividendRepository.saveAll(anyList()))
                .willReturn(List.of(new Dividend(companyId, dividendDto)));

        //when
        CompanyDto dto = companyService.save(ticker);
        ArgumentCaptor<Company> companyCaptor = ArgumentCaptor.forClass(Company.class);

        //then
        verify(companyRepository, times(1)).save(companyCaptor.capture());
        verify(dividendRepository, times(1)).saveAll(dividendCaptor.capture());

        assertThat(companyCaptor.getValue().getName()).isEqualTo(name);
        assertThat(companyCaptor.getValue().getTicker()).isEqualTo(ticker);
        assertThat(dividendCaptor.getValue().get(0).getDividend()).isEqualTo(divide);
        assertThat(dividendCaptor.getValue().get(0).getDate()).isEqualTo(time);
        assertThat(dto.getName()).isEqualTo(name);
        assertThat(dto.getTicker()).isEqualTo(ticker);
    }

    @DisplayName("검색한 ticker가 DB에 존재하는 경우 저장 실패")
    @Test
    void failedSave1() {
        String ticker = "ticker";

        //given
        given(companyRepository.existsByTicker(anyString())).willReturn(true);

        //when
        assertThatThrownBy(() -> companyService.save(ticker))
                .isInstanceOf(AlreadyExistTickerException.class);

        //then
    }

    @DisplayName("회사명이 없는 경우 저장 실패")
    @Test
    void failedSave2() {
        String ticker = "ticker";

        //given
        given(companyRepository.existsByTicker(anyString())).willReturn(false);
        given(scraper.scrapCompanyByTicker(anyString())).willReturn(null);

        //when
        assertThatThrownBy(() -> companyService.save(ticker))
                .isInstanceOf(NoCompanyException.class);

        //then
    }

    @DisplayName("기업 정보 전체 조회")
    @Test
    void getAllCompany() {
        String ticker = "ticker";
        String name = "companyName";

        CompanyDto companyDto = new CompanyDto(ticker, name);

        Pageable pageable = Pageable.ofSize(2);
        Page<Company> page = new PageImpl<>(List.of(new Company(companyDto), new Company(companyDto)), pageable, 3);

        //given
        given(companyRepository.findAll(any(Pageable.class)))
                .willReturn(page);

        //when
        Page<Company> companies = companyService.getAllCompany(pageable);

        //then
        assertThat(companies.getTotalPages()).isEqualTo(2);
        assertThat(companies.getContent().size()).isEqualTo(2);
        assertThat(companies.getTotalElements()).isEqualTo(3);
        assertThat(companies.getContent().get(0).getName()).isEqualTo(name);
        assertThat(companies.getContent().get(0).getTicker()).isEqualTo(ticker);
    }

    @DisplayName("prefix로 시작하는 기업들 조회")
    @Test
    void getCompanyNamesByKeyword() {
        String ticker = "ticker";
        String name = "name";

        CompanyDto companyDto = new CompanyDto(ticker, name);

        //given
        given(companyRepository.findByNameStartingWithIgnoreCase(anyString(), any()))
                .willReturn(List.of(new Company(companyDto)));

        //when
        List<String> companies = companyService.getCompanyNamesByKeyword("n");

        //then
        assertThat(companies.size()).isEqualTo(1);
        assertThat(companies.get(0)).isEqualTo(name);
    }

    @DisplayName("기업 삭제")
    @Test
    void deleteCompany() {
        String ticker = "ticker";
        String name = "companyName";
        Long id = 1L;

        //given
        given(companyRepository.findByTicker(anyString()))
                .willReturn(Optional.of(new Company(id, ticker, name)));
        willDoNothing().given(dividendRepository).deleteAllByCompanyId(anyLong());
        willDoNothing().given(companyRepository).delete(any());
        willDoNothing().given(companyService).deleteAutocompleteKeyword(anyString());

        //when
        String companyName = companyService.deleteCompany(ticker);

        //then
        assertThat(companyName).isEqualTo(name);
    }
    
    @DisplayName("기업 삭제 실패")
    @Test
    void failedDeleteCompany() {
        String ticker = "ticker";

        //given
        given(companyRepository.findByTicker(ticker)).willThrow(new NoCompanyException());

        //when
        assertThatThrownBy(() -> companyService.deleteCompany(ticker))
                .isInstanceOf(NoCompanyException.class);
        
        //then
    }
}
