# BoardCraft — MVP 범위 고정(Freeze) & 트레이스 매트릭스 v0.2

## 0) 식별자 규칙

* 요구사항: **REQ-###** (예: REQ-001)
* 유스케이스: **UC-#** (예: UC-1 게시글 등록)
* API 테스트: **T-API-###**
* 단위/통합 테스트: **T-UNIT-### / T-INT-###**
* 수용 기준: **AC-REQ-###-n** (요구사항별 일련번호)

---

## 1) Must-Have 요구사항 잠금(Freeze)

1. **REQ-001 인증/인가 – 회원가입·로그인 (이메일+비밀번호)**
2. **REQ-002 게시글 CRUD + Markdown + 카테고리(1) + 태그(N) + 첨부파일**
3. **REQ-003 댓글/대댓글(1-레벨)**
4. **REQ-004 목록 페이지네이션/정렬/키워드 검색(제목/본문/태그/작성자)**
5. **REQ-005 RBAC 역할( GUEST / USER / MOD / ADMIN )**
6. **REQ-006 소프트 삭제(복구 가능 보관함)**
7. **REQ-007 기본 모더레이션(신고/숨김/블럭)**
8. **REQ-008 관측성(구조적 로그, TraceId, 메트릭: 요청수/p95/에러율)**

---

## 2) 트레이스 매트릭스 (요구사항 ↔ 유스케이스 ↔ API ↔ 테스트)

| Req     | 관련 유스케이스                                       | 대표 API 엔드포인트                                            | 연결된 테스트 아티팩트                                                                               |                                              |
| ------- | ---------------------------------------------- | ------------------------------------------------------- | ------------------------------------------------------------------------------------------ | -------------------------------------------- |
| REQ-001 | UC-1 회원가입, 로그인                                 | `POST /auth/signup`, `POST /auth/signin`                | **T-UNIT-001\~011**                                                                        |                                              |
| REQ-002 | UC-2 글 작성, UC-3 단건 조회, UC-4 수정/재발행, UC-5 삭제/복구 | `POST /api/v1/posts`, `GET /api/v1/posts/{slug}`, \`PUT | PATCH /api/v1/posts/{id}`, `DELETE /api/v1/posts/{id}`, `POST /api/v1/posts/{id}/restore\` | **T-API-2xx**, **T-UNIT-2xx**, **T-INT-2xx** |

> REQ-003\~008은 정의만 되어 있고 테스트 미작성 상태. 추후 작성 시 ID 부여 예정.

---

## 3) 수용 기준 (Acceptance Criteria)

### REQ-001 인증/인가 – 회원가입·로그인

* **AC-REQ-001-1**: `POST /auth/signup`에 유효 이메일/비밀번호/닉네임 제출 시 201 응답.
* **AC-REQ-001-2**: 이메일 또는 닉네임 중복 시 400 VALIDATION\_ERROR + validation map 응답.
* **AC-REQ-001-3**: `POST /auth/login` 성공 시 세션 저장, 실패 시 401.
* **AC-REQ-001-4**: 비밀번호는 BCrypt로 저장, 평문 저장 금지(단위 테스트로 검증).

### REQ-002 게시글 CRUD + Markdown + Taxonomy + 첨부

#### 생성(Create)

* **AC-REQ-002-1**: `POST /api/v1/posts` 유효 요청 시 **201 Created** + `Location: /api/v1/posts/{slug}` 반환. 응답 바디엔 `id, slug, title, contentMd, categoryId?, tags[], isPublished, publishedAt?, createdAt, updatedAt` 포함.
* **AC-REQ-002-2**: **슬러그**는 `title`로부터 규칙(한글/영문/숫자/하이픈, 연속 하이픈 압축, 200자 제한, 앞뒤 하이픈 제거)으로 생성. 중복 시 `-2, -3…` 증분. 최종적으로 DB 유니크 충족.
* **AC-REQ-002-3**: `categoryId` 제공 시 **존재하는 카테고리**여야 함. 없으면 400 `VALIDATION_ERROR { categoryId: ... }`.
* **AC-REQ-002-4**: `tags[]`는 서버에서 **정규화(트림, 중복 제거, 길이 제한, 소문자 또는 원문 유지 정책 중 택1)** 후 **없는 태그는 생성**하고, `post_tags`로 연결.
* **AC-REQ-002-5**: `isPublished=true`로 생성 시 `publishedAt`은 서버 시간이 밀리초(3) 정밀도로 세팅. 초안(`false`)이면 `publishedAt=null`.
* **AC-REQ-002-6**: 첨부파일 업로드는 최대 **N개(예: 5)**, 각 **최대 M MB(예: 10MB)**, **허용 MIME**만 저장. 위반 시 400 `VALIDATION_ERROR { attachments: ... }`.
* **AC-REQ-002-7**: 권한: **로그인 사용자만 작성 가능**. 비로그인 401. 정지/차단 사용자는 403.

#### 조회(Read)

* **AC-REQ-002-8**: `GET /api/v1/posts/{slug}`: 공개 글은 누구나 200. 미발행/숨김/삭제 글은 **작성자 본인 또는 MOD/ADMIN**만 200, 그 외 404로 위장.
* **AC-REQ-002-9**: 서버 렌더링 제공 시 `renderedHtml`은 **CommonMark 호환** 렌더러 + **HTML sanitize** 적용. (클라이언트 렌더 선택 가능)
* **AC-REQ-002-10**: 첨부는 메타(`id, filename, size, contentType, url`)로 노출. 비공개 스토리지면 서명 URL/프록시 규약 준수.

#### 수정(Update)

* **AC-REQ-002-11**: `PUT /api/v1/posts/{id}` 또는 `PATCH` 허용. **작성자 본인 또는 MOD/ADMIN**만 200/204. 권한 없으면 403.
* **AC-REQ-002-12**: **슬러그 정책** — 제목 변경 시 기본 **불변**. `changeSlug=true`일 때만 재할당, 기존 슬러그는 `post_slugs` 히스토리에 보관(301 리다이렉트). 충돌 시 증가 접미사 규칙 적용.
* **AC-REQ-002-13**: 카테고리 변경 시 존재 검증. 태그 교체 시 정규화·업서트 동일 적용.
* **AC-REQ-002-14**: 초안→발행 전환 시 `publishedAt`이 비어 있으면 **그 순간으로 세팅**. 발행→초안 회귀는 **MVP 제외**(요청 시 400).

#### 삭제(Delete)

* **AC-REQ-002-15**: `DELETE /api/v1/posts/{id}`는 **소프트 삭제**만 수행(`is_deleted=1`, `deletedAt`이 있다면 세팅). **204 No Content**.
* **AC-REQ-002-16**: 삭제된 글은 기본 조회/목록에서 제외. **복구 API** `POST /api/v1/posts/{id}/restore`로 복구 가능(REQ-006 연동).

#### 목록/필터(최소 보장)

* **AC-REQ-002-17**: 기본 목록 `GET /api/v1/posts`는 **is\_published=1 AND is\_hidden=0 AND is\_deleted=0**만 노출. 작성자/관리자는 본인 글을 포함한 가시성 모드 제공 `visibility=mine|all`.
* **AC-REQ-002-18**: `category`·`tag`·`author`·`q`(제목/본문 부분일치) 조합 필터 지원. 불가 항목은 400.

#### 유효성/제약

* **AC-REQ-002-19**: `title`(1..160), `contentMd`(1..MEDIUMTEXT), `slug`(1..200) 길이 검증. 실패 시 400 `VALIDATION_ERROR` with field map.
* **AC-REQ-002-20**: **DB 유니크**(`uk_posts_slug`) 위반은 409 `CONFLICT {"field":"slug"}`로 매핑. 서비스는 충돌 방지 시도 후, 경합은 DB 예외 캐치로 재시도 수행.
* **AC-REQ-002-21**: FK 제약(`author_id`, `category_id`) 위반은 400으로 매핑하고 필드 지목.

#### 감사/메타

* **AC-REQ-002-22**: 모든 성공 응답에 `createdAt`, `updatedAt`(ms 정밀도) 포함. 발행 글만 `publishedAt` 포함.
* **AC-REQ-002-23**: 생성/수정/삭제 시 구조적 로그에 `traceId`, `userId`, `postId`, `action`, `status` 기록(REQ-008 연동).

#### 보안/하드닝

* **AC-REQ-002-24**: Markdown 렌더 시 스크립트/이벤트 핸들러/위험 태그 제거. 코드블록/이미지/링크 허용 범위 명시. 외부 링크는 `rel="noopener noreferrer"`.
* **AC-REQ-002-25**: 첨부 URL은 **권한 검증**을 통과해야 접근 가능(서명 URL 만료시간 T분, 예: 10분).

---

## 4) 스코프 아웃(명확화)

* WYSIWYG/MD 하이브리드 에디터, 버전 히스토리, 자동저장/협업 편집은 **MVP 제외**.
* 발행 후 슬러그 변경은 **옵션 플래그 있을 때만** 허용. 기본은 불변.
* 외부 스토리지 미러링/이미지 리사이즈 파이프라인은 **후속 스프린트**.
