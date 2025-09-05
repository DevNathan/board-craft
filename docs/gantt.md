```mermaid
gantt
    title BoardCraft 프로젝트 마일스톤 (30분/일 기준)
    dateFormat  YYYY-MM-DD
    axisFormat  %m-%d

    section M1
    요구사항 및 프로젝트 골격 :a1, 2025-09-04, 14d

    section M2
    인증 및 사용자 관리      :a2, after a1, 21d

    section M3
    게시글 CRUD 및 소프트 삭제 :a3, after a2, 21d

    section M4
    댓글 및 모더레이션        :a4, after a3, 21d

    section M5
    관측성 및 배포            :a5, after a4, 14d

    section M6 (선택)
    기능 확장                 :a6, after a5, 14d
```