package com.zerobase.stockservice.controller;

import com.zerobase.stockservice.domain.Company;
import com.zerobase.stockservice.dto.CompanyDto;
import com.zerobase.stockservice.dto.constants.CacheKey;
import com.zerobase.stockservice.exception.impl.NoParamException;
import com.zerobase.stockservice.service.CompanyService;
import lombok.AllArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/company")
@AllArgsConstructor
public class CompanyController {
    private final CompanyService companyService;
    private final CacheManager redisCacheManager;

    //검색하고자 하는 prefix 를 입력으로 받고, 해당 prefix 로 검색되는 회사명 리스트 중 10개 반환
    @GetMapping("/autocomplete")
    @PreAuthorize("hasRole('READ')")
    public ResponseEntity<?> autocomplete(@RequestParam String keyword) {
        List<String> result = this.companyService.getCompanyNamesByKeyword(keyword);
        return ResponseEntity.ok(result);
    }

    //서비스에서 관리하고 있는 모든 회사 목록을 반환
    @GetMapping
    @PreAuthorize("hasRole('READ')")
    public ResponseEntity<?> searchCompany(final Pageable pageable) {
        Page<Company> companies = this.companyService.getAllCompany(pageable);
        return ResponseEntity.ok(companies);
    }

    //새로운 회사 정보 추가
    @PostMapping
    @PreAuthorize("hasRole('WRITE')")
    public ResponseEntity<?> addCompany(@RequestBody CompanyDto request) {
        String ticker = request.getTicker().trim();
        if (ObjectUtils.isEmpty(ticker)) {
            throw new NoParamException();
        }

        CompanyDto companyDto = this.companyService.save(ticker);
        return ResponseEntity.ok(companyDto);
    }

    //ticker 에 해당하는 회사 정보 삭제
    @DeleteMapping("/{ticker}")
    @PreAuthorize("hasRole('WRITE')")
    public ResponseEntity<?> deleteCompany(@PathVariable String ticker) {
        String companyName = this.companyService.deleteCompany(ticker);
        this.clearFinanceCache(companyName);
        return ResponseEntity.ok(companyName);
    }

    public void clearFinanceCache(String companyName) {
        this.redisCacheManager.getCache(CacheKey.KEY_FINANCE).evict(companyName);
    }
}
