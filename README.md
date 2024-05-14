# 주식 배당금 정보 조회 서비스 API

## Development Environment
- Intellij IDEA Community
- Java 17
- Gradle 7.2
- Spring Boot 2.5.6

## Tech Stack
- Spring Boot, Spring Security, Spring Data Jpa
- H2
- Redis
- Jsoup
- JWT
- lombok

## API
- ### /auth
  <details>
  <summary>회원가입 API</summary>
  
  - POST /signup
  - 중복 ID는 허용하지 않음
  - 패스워드는 암호화된 형태로 저장됨
  </details>
  <details>
  <summary>로그인 API</summary>

    - POST /signin
    - 로그인 API
    - 회원가입이 되어있고, 아이디/패스워드가 일치하는 경우 JWT 발급
  </details>
  <br>
- ### /company
  <details>
  <summary>회사명 검색 API</summary>

    - GET /autocomplete
    - 검색하고자 하는 prefix를 입력값으로 받고, 해당 prefix로 검색되는 회사명 리스트 중 10개 반환
  </details>
  <details>
  <summary>회사 목록 확인 API</summary>

    - GET
    - 서비스에서 관리하고 있는 모든 회사 목록을 반환
    - 반환 결과는 Page 인터페이스 형태
  </details>
  <details>
  <summary>회사 정보 추가 API</summary>

    - POST
    - 추가하고자 하는 회사의 ticker를 입력받아 해당 회사 정보를 스크래핑, 저장
    - 이미 보유하고 있는 회사의 경우 400 status 코드와 에러메세지 반환
    - 존재하지 않는 회사 ticker일 경우 400 status 코드와 에러메세지 반환
  </details>
  <details>
  <summary>회사 정보 삭제 API</summary>

    - DELETE /{ticker}
    - ticker에 해당하는 회사 정보 삭제
    - 삭제 시 회사의 배당금 정보와 캐시도 모두 삭제
  </details>
  <br>
- ### /finance
  <details>
  <summary>배당금 정보 확인 API</summary>

    - GET /dividend/{companyName}
    - 회사명을 받아 회사 메타 정보와 배당금 정보를 반환
    - 잘못된 회사명이 입력으로 들어온 경우 400 status 코드와 에러메세지 반환
  </details>
