# SpringBoot Part3 Weekly Mission

## 요구사항

- SpringBoot Part3 Weekly Mission

  **(기본) 바우처 서비스 관리페이지 개발하기**

  - Spring MVC를 적용해서 thymeleaf 템플릿을 설정해보세요.

  - 커맨드로 지원했던 기능을 thymeleaf를 이용해서 관리페이지를 만들고 다음 기능을 지원가능하게 해보세요

    - [x] 조회페이지

    - [x] 상세페이지

    - [x] 입력페이지

    - [x] 삭제기능

  **(기본) 바우처 서비스의 API 개발하기**

  - Spring MVC를 적용해서 JSON과 XML을 지원하는 HTTP API를 개발해보세요

    - [x] 전체 조회기능

    - [x] 조건별 조회기능 (바우처 생성기간 및 특정 할인타입별)

    - [x] 바우처 추가기능

    - [x] 바우처 삭제기능

    - [x] 바우처 아이디로 조회 기능

  - [ ] **(보너스) 바우처 지갑용 관리페이지를 만들어보세요.**
  

-----

## 추가된 기능 설명

- Type, Date, Type And Date 이용하여 바우처를 검색할 수 있습니다.
  - 관리자 페이지에서 타입을 클릭하거나, 달력을 통해 기간을 입력하여 검색 가능하게 하였습니다.

- Converter를 구현, 등록하여 url 파라미터로 온 String 값을 Date로 자동 변환, 검색을 수행할 수 있게 도움
- message 기능 이용하여 일관된 표현 사용
- bean validation으로 비어 있는 필드 찾음
- ControllerAdvice 이용하여 에러 페이지, 에러 응답(API) 처리

-----

## PR 포인트

- 메인 페이지(index.html)에 테스트 데이터 입력 기능을 넣어 두었습니다.
  - 이로 인해 VoucherController에 관련 로직이 있으며, VoucherRepository에 대한 의존성을 갖습니다.
  - PR 등의 용이성을 위한 장치로, 이 부분은 실제 출시라고 가정했을 때 삭제된다고 생각하시면 됩니다.



### PR포인트1:  컨트롤러가 비대해졌을 때 어떤 기준을 갖고 분리하시나요?

- 컨트롤러 로직이 생각보다 길어져서 분리해야 되나 고민이 되었습니다. 하지만 Voucher의 CRD라는 공통 주제에 대한 컨트톨러를 분할하는 것이 적합할까 고민이 들어 일단 한 클래스 내에 몰아 넣었습니다.
- 이렇게 컨트롤러가 비대해 졌을 경우, 분리하시는 편이신가요? 아니면 같이 뭉쳐 있어야 한다고 판단하여, 분리하지 않으시는 편이신가요?



### PR 포인트2:  검증을 어디서 진행할 것인지

- 입력 값 검증하고, 이상이 있을 경우 bindingResult 기능 이용하여 리다이렉트 하면서 폼에 입력한 값을 유지, 에러 정보를 표시하고 있습니다.
- 이 방식을 사용하다 보니 검증이 자연스럽게 컨트롤러 단에서 이루어지게 되었는데, 이런 방식이 적절할 지 감이 잘 오지 않습니다.
- 주요 도메인 모델에 대한 생성/수정 등의 값 검증을 보통 어디서 수행하는지, 그 이유는 무엇인지 궁금합니다.



### PR포인트3: 검색 조건이 추가된다면 하나의 GetMapping으로 처리할 것인지?

- 과제 요구사항은 여러 방식으로 바우처를 검색할 수 있어야 한다고 합니다.
  - 이 각각의 검색 조건들을 구현하려다 보니, voucherList를 처리하는 하나의 메서드에서
    @RequestParam(required = false)인 여러 매개변수를 갖게 하여 처리하였습니다만,
    다수의 매개변수로 인하여 코드가 난잡해지지 않았나 생각했습니다.
  - 검색 조건이 다양해서 이렇게 매개변수가 난잡해질 경우, 어떻게 코드 가독성을 갖출 수 있을지 멘토님의 의견이 궁금합니다!



### PR 포인트 4: 예외 정보 노출에 관하여

- 에러 페이지나 예외 발생 시 API 응답에는 세부적인 예외 정보를 넣지 않았습니다. 사용자 입장에서 예외에 대한 세부 정보를 알면 안 된다고 생각하여 최대한 간소한 정보만 전달하고 로그를 찍기만 했습니다.
-  실무에서는 어느 정도로 예외 발생 시 정보를 노출하는지가 궁금합니다.

-----

## 마지막으로

- MVC, REST에 대한 이해가 부족하여 적절하게 만들었는지, 어떤 부분이 부족한지조차 아직 많이 부족한 상황이라고 느꼈습니다.
  - 그런 이유로 요구사항은 REST API구현이 아닌 HTTP API 구현이라고 일단 명시하기는 했습니다.
  - 타임리프에 대한 이해도 많이 부족하다고 느꼈는데, 실무에서 백엔드 엔지니어가 SSR 기술에 대해 어떤 것을 갖춰야 할지 아직 이해가 부족한 상황입니다.
- 그런 이유로, 혹시 이번 주차 과제에 관해서 특별히 이건 좀 알았으면 좋겠다 싶은 개념이나, 책이 있으시다면 소개해주시면 감사하겠습니다.

-----

### 항상 도움이 되는 조언과 리뷰 감사드립니다. 이번 한 주 특히 가족들과 함께 건강하고 행복하게 보내실 수 있으시면 좋겠습니다. 감사합니다.