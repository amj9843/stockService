package com.zerobase.stockservice.scraper;

import com.zerobase.stockservice.dto.CompanyDto;
import com.zerobase.stockservice.dto.ScrapedResult;

public interface Scraper {
    CompanyDto scrapCompanyByTicker(String ticker);
    ScrapedResult scrap(CompanyDto company);
}
