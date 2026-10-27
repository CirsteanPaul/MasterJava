# Lab 3 – Dependency Injection and Cross-Cutting Concerns

Requires JDK 21+ and Maven. Spring Boot 3.5, Drools 10.

| Part | Folder | Run |
|---|---|---|
| Compulsory | `compulsory/` | `mvn spring-boot:run` |
| Homework + Advanced | `library/` | `mvn spring-boot:run -Dspring-boot.run.arguments=--library.borrowing.policy=rules` |

`mvn test` in each folder runs the tests.

## Compulsory – injection points and their order

`ReportGenerator` uses every major injection point:

| Point | Where |
|---|---|
| Constructor | `ReportGenerator(DataSourceService, InjectionLog)` |
| Field | `@Autowired FormatterService formatter`, `@Value String title` |
| Setter | `setNotifier(Notifier)` (`@Primary`), `setBackupNotifier(@Qualifier("sms") Notifier)` |
| Method | `@Autowired prepare(AuditTrail, List<Notifier>, ObjectProvider<MissingService>)` (collection + optional) |
| `@Bean` method parameters | `AppConfig.reportScheduler(ReportGenerator, @Value String)` |

Each step writes a snapshot of which dependencies are already set into `InjectionLog`:

```
1. CONSTRUCTOR    dataSource injected; [constructor=✓, field=-, setter=-, method=-]
2. FIELD          formatter=FormatterService, title='Monthly sales report' (set after constructor, before this step)
3. SETTER         notifier=EmailNotifier; [constructor=✓, field=✓, setter=✓, method=-]
4. SETTER         backupNotifier=SmsNotifier; [constructor=✓, field=✓, setter=✓, method=-]
5. METHOD         auditTrail + 2 notifiers + optional MissingService (present=false); [...✓]
6. POST_CONSTRUCT bean fully initialised; [...✓]
```

The constructor sees no field set, and the first setter sees the field already set. That proves
**constructor → field → setter & method** (`AutowiredAnnotationBeanPostProcessor` injects fields before methods),
followed by `@PostConstruct`. `InjectionOrderTest` checks this.

## Homework – Library Management System (`library/`)

* **Domain and in-memory repositories:** `Member`, `Book`, `Loan`, `MembershipType`, `BookType`, plus
  `InMemory{Member,Book,Loan}Repository`.
* **Pluggable `BorrowingPolicy`:** the custom property `library.borrowing.policy` picks it
  (`@ConditionalOnProperty`, typed in `LibraryProperties`):
  * `fixed` (default): `FixedLimitBorrowingPolicy`, same limit for everyone (`library.borrowing.fixed-limit`)
  * `role-based`: `RoleBasedBorrowingPolicy`, limit and loan period per role (`role-limits`, `role-loan-days`)
  * `rules`: `RuleEngineBorrowingPolicy`, Drools (Advanced)
* **`BorrowingService`:** constructor injection only. It validates through the injected policy, creates the loan
  and publishes `HeavyBorrowerEvent` when a member holds more than `library.events.heavy-borrower-threshold` books.
  `HeavyBorrowerListener` handles the event.
* **Aspects** (`lab3.library.aop`, nested with `@Order`):
  1. `LoanLoggingAspect` (`@Around`): method name, member name, book title, execution time
  2. `BorrowingErrorAspect` (`@AfterThrowing`): failed operation, its arguments and the error message
  3. `BorrowingValidationAspect` (`@Before` borrow): member exists, book available, limit not exceeded. Throws
     `MemberNotFoundException`, `BookNotFoundException`, `BookNotAvailableException`, `BorrowingLimitExceededException`

  Because the error aspect wraps the validation aspect, validation failures are logged too.

`DemoRunner` seeds data and runs a scenario. Compare its output for `fixed`, `role-based` and `rules`.

## Advanced – Drools rules

The rules are in `src/main/resources/rules/*.drl`:

* `01-limits.drl`: max books per membership (student 3, regular 5, premium 10)
* `02-eligibility.drl`: availability, limit, reference books stay in the library, rare books are premium-only,
  students holding 2+ books can't take new releases
* `03-loan-periods.drl`: base period per membership (14/21/30 days), capped for new releases (7) and rare books (10)

`RuleEngineBorrowingPolicy` inserts a `BorrowingFacts` fact, fires the rules and maps the result to a
`BorrowingDecision`. If no rule sets a limit or loan period, the request is denied (safe default).
`BorrowingService` doesn't know Drools exists.

To change rules without rebuilding, copy the DRL files to a folder and point the app at it:

```
java -jar target/library-management-1.0.0.jar --library.borrowing.policy=rules \
     --library.rules.location='file:./rules/*.drl' --library.rules.auto-reload=true
```

With `auto-reload`, rule files that are added or edited are recompiled on the next evaluation.
`RuleEngineBorrowingPolicyTest.rulesCanBeAddedAtRuntimeWithoutCodeChanges` shows this.
A new membership type only needs a new enum constant and its rules.
