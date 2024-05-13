package com.zerobase.stockservice.scraper;

import com.zerobase.stockservice.dto.CompanyDto;
import com.zerobase.stockservice.dto.DividendDto;
import com.zerobase.stockservice.dto.ScrapedResult;
import com.zerobase.stockservice.dto.constants.Month;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class YahooFinanceScraper implements Scraper{
    private static final String STATIC_URL = "https://finance.yahoo.com/quote/%s/history?period1=%d&period2=%d&interval=1mo";
    private static final String SUMMARY_URL = "https://finance.yahoo.com/quote/%s?p=%s";
    private static final long START_TIME = 86400; // 60*60*24

    @Override
    public ScrapedResult scrap(CompanyDto company) {
        long now = System.currentTimeMillis() / 1000;

        ScrapedResult scrapResult = new ScrapedResult();
        scrapResult.setCompanyDto(company);

        String url = String.format(STATIC_URL, company.getTicker(), START_TIME, now);

        try {
            Connection connection = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 6.2; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1667.0 Safari/537.36")
                    .header("scheme", "https")
                    .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                    .header("accept-encoding", "gzip, deflate, br")
                    .header("accept-language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7,es;q=0.6")
                    .header("cache-control", "no-cache")
                    .header("pragma", "no-cache")
                    .header("upgrade-insecure-requests", "1");
            Document document = connection.timeout(30000).get();

            Elements parsingDivs = document.getElementsByAttributeValue("class", "table svelte-ewueuo");
            Element tableEle = parsingDivs.get(0);

            Element tbody = tableEle.children().get(1);
            List<DividendDto> dividends = new ArrayList<>();
            for (Element e: tbody.children()) {
                String txt = e.text();

                if (!txt.endsWith("Dividend")) continue;

                String[] splits = txt.split(" ");
                int month = Month.strToNumber(splits[0]);
                int day = Integer.valueOf(splits[1].replace(",", ""));
                int year = Integer.valueOf(splits[2]);
                String dividend = splits[3];

                if (month < 0) {
                    throw new RuntimeException("Unexpected Month enum value -> " + splits[0]);
                }

                dividends.add(
                        new DividendDto(
                                LocalDateTime.of(year, month, day, 0, 0),
                                dividend)
                );
            }
            scrapResult.setDividends(dividends);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return scrapResult;
    }

    @Override
    public CompanyDto scrapCompanyByTicker(String ticker) {
        String url = String.format(SUMMARY_URL, ticker, ticker);

        try {
            Document document = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 6.2; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1667.0 Safari/537.36")
                    .header("scheme", "https")
                    .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                    .header("accept-encoding", "gzip, deflate, br")
                    .header("accept-language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7,es;q=0.6")
                    .header("cache-control", "no-cache")
                    .header("pragma", "no-cache")
                    .header("upgrade-insecure-requests", "1")
                    .timeout(30000)
                    .get();
            Element titleEle = document.getElementsByTag("h1").get(1);
            String title = titleEle.text().split("\\(")[0].trim();

            return new CompanyDto(ticker, title);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
