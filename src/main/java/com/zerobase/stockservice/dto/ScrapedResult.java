package com.zerobase.stockservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class ScrapedResult {
    private CompanyDto companyDto;
    private List<DividendDto> dividends;

    public ScrapedResult() {
        this.dividends = new ArrayList<>();
    }
}
