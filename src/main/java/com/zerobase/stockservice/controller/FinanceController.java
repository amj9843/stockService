package com.zerobase.stockservice.controller;

import com.zerobase.stockservice.dto.ScrapedResult;
import com.zerobase.stockservice.service.FinanceService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/finance")
@AllArgsConstructor
public class FinanceController {
    private final FinanceService financeService;

    //회사 이름을 인풋으로 받아서 해당 회사의 메타 정보와 배당금 정보를 반환
    @GetMapping("/dividend/{companyName}")
    @PreAuthorize("hasRole('READ')")
    public ResponseEntity<?> searchFinance(@PathVariable String companyName) {
        ScrapedResult result = this.financeService.getDividendByCompanyName(companyName);
        return ResponseEntity.ok(result);
    }
}
