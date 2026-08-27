# Developer Guide for AI Models

## 🎯 Purpose

This guide is designed to help AI models and developers understand the Security Intelligence Platform codebase, architecture, and development patterns. It provides context for making informed decisions when implementing features, fixing bugs, or extending functionality.

## 🏗️ Architecture Overview

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Security Intelligence Platform              │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌──────────────┐         ┌──────────────┐                  │
│  │   Frontend   │         │   Backend    │                  │
│  │   (Angular)  │◄────────┤  (Spring Boot)│                  │
│  └──────────────┘         └──────────────┘                  │
│         │                        │                          │
│         │                        │                          │
│         │                 ┌──────▼──────┐                   │
│         │                 │  MongoDB    │                   │
│         │                 │  Database   │                   │
│         │                 └─────────────┘                   │
│         │                        │                          │
│         │                 ┌──────▼──────┐                   │
│         │                 │ Gemini AI   │                   │
│         │                 │   Service   │                   │
│         │                 └─────────────┘                   │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### Technology Stack

**Backend:**
- Java 20/21
- Spring Boot 3.2.0
- Spring Data MongoDB
- Jackson (JSON processing)
- JUnit 5 + Mockito (Testing)
- RestTemplate (HTTP client)

**Frontend:**
- Angular 17 (Standalone Components)
- TypeScript
- RxJS (Reactive programming)
- Angular HttpClient

**Database:**
- MongoDB 7.0

**External Services:**
- Google Gemini API (AI guidance)

## 📁 Project Structure

### Backend Structure

```
backend/src/main/java/com/securityintel/
├── ai/                          # AI integration
│   └── GeminiAiService.java     # Gemini API integration
├── comparison/                  # Scan comparison logic
│   └── ScanComparisonEngine.java
├── config/                      # Configuration
│   ├── MongoConfig.java         # MongoDB configuration
│   └── RestTemplateConfig.java  # HTTP client configuration
├── controller/                  # REST API endpoints
│   ├── AiAssistantController.java
│   ├── DashboardController.java
│   ├── FindingController.java
│   ├── GitHubActionsIntegrationController.java
│   ├── RemediationController.java
│   ├── ReportController.java
│   ├── ScanExecutionController.java
│   └── ServiceController.java
├── deduplication/               # Finding deduplication
│   ├── DeduplicationEngine.java
│   └── DeduplicationResult.java
├── dto/                        # Data Transfer Objects
│   ├── DashboardSummaryDto.java
│   ├── ScanReportDto.java
│   ├── SecurityFindingDto.java
│   └── ServiceDto.java
├── exception/                   # Exception handling
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   └── DatabaseException.java
├── mapper/                     # Entity mapping
│   └── EntityMapper.java
├── model/                      # Domain entities
│   ├── SecurityFinding.java
│   ├── Service.java
│   ├── ScanExecution.java
│   ├── RemediationItem.java
│   └── Enums (Priority, Status, Environment, etc.)
├── normalization/              # Report normalization
│   └── FindingNormalizer.java
├── parser/                     # Scanner report parsing
│   ├── SecurityReportParser.java
│   ├── TrivyReportParser.java
│   ├── SnykReportParser.java
│   └── ReportParserFactory.java
├── prioritization/             # Risk calculation
│   ├── SecurityPrioritizationEngine.java
│   └── PriorityResult.java
├── remediation/                # Remediation management
│   └── RemediationService.java
├── repository/                 # Database repositories
│   ├── SecurityFindingRepository.java
│   ├── ServiceRepository.java
│   ├── ScanExecutionRepository.java
│   └── RemediationItemRepository.java
├── scan/                       # Scan execution management
│   └── ScanExecutionService.java
├── securitystate/              # Security state calculation
│   └── SecurityStateCalculator.java
├── service/                    # Business logic services
│   ├── DashboardService.java
│   ├── SecurityReportService.java
│   ├── SecurityFindingService.java
│   └── ServiceManagementService.java
└── SecurityIntelligencePlatformApplication.java
```

### Frontend Structure

```
frontend/src/app/
├── models/                     # TypeScript models
│   └── dashboard.model.ts
├── pages/                      # Page components
│   ├── dashboard/              # Main dashboard
│   ├── remediation/            # Remediation items
│   ├── findings/               # Security findings
│   ├── finding-detail/         # Individual finding details
│   ├── scans/                  # Scan history
│   ├── services/               # Service catalog
│   └── ai-assistant/           # AI chat interface
├── services/                   # API services
│   └── api.service.ts
├── app.component.ts           # Root component
├── app.routes.ts               # Routing configuration
└── styles.css                  # Global styles
```

## 🔑 Core Concepts

### 1. Security Finding Lifecycle

```
Scanner Report → Normalization → Deduplication → Prioritization → Remediation Item
```

**Key Stages:**
1. **Ingestion**: Raw scanner reports uploaded via API
2. **Normalization**: Different scanner formats converted to unified `SecurityFinding` model
3. **Deduplication**: Findings correlated across tools using fingerprinting
4. **Prioritization**: Risk scores calculated based on business context
5. **Remediation**: Actionable items created for engineering teams

### 2. Priority Calculation (P0-P4)

**Priority Levels:**
- **P0**: Critical - Internet-exposed production services with sensitive data
- **P1**: High - Production services or internet-exposed development services
- **P2**: Medium - Development services with moderate business impact
- **P3**: Low - Low business impact or non-direct exposure
- **P4**: Informational - Minimal security impact

**Risk Score Formula:**
```
Risk Score = (CVSS Score × 10) + 
             (Business Criticality Weight) + 
             (Internet Exposure Weight) + 
             (Data Sensitivity Weight) + 
             (Environment Weight)
```

### 3. Scan Comparison Logic

**Detection States:**
- **NEW**: Found in latest scan, not in previous scan
- **PRESENT (UNCHANGED)**: Found in both current and previous scans
- **NOT_DETECTED_IN_LATEST_SCAN**: Found in previous scan, not in latest scan

**Important:** Findings marked as `NOT_DETECTED_IN_LATEST_SCAN` are not automatically resolved - requires explicit status update.

### 4. Security State Calculation

**Service States:**
- **HEALTHY**: Fresh scan, no P0/P1 open findings
- **ATTENTION**: Has open P1 or significant P2 findings
- **CRITICAL**: Has open P0 findings
- **STALE**: Latest production scan older than 24 hours
- **UNKNOWN**: No successful scan exists

## 🔧 Development Patterns

### Backend Patterns

#### 1. Controller Pattern

```java
@RestController
@RequestMapping("/api/endpoint")
@CrossOrigin(origins = "*")
public class ExampleController {
    
    private final ExampleService exampleService;
    
    public ExampleController(ExampleService exampleService) {
        this.exampleService = exampleService;
    }
    
    @GetMapping
    public ResponseEntity<List<Example>> getAll() {
        return ResponseEntity.ok(exampleService.findAll());
    }
    
    @PostMapping
    public ResponseEntity<Example> create(@RequestBody Example example) {
        return ResponseEntity.ok(exampleService.save(example));
    }
}
```

#### 2. Service Pattern

```java
@Service
public class ExampleService {
    
    private final ExampleRepository repository;
    
    public ExampleService(ExampleRepository repository) {
        this.repository = repository;
    }
    
    public List<Example> findAll() {
        return repository.findAll();
    }
    
    public Example save(Example example) {
        return repository.save(example);
    }
}
```

#### 3. Repository Pattern

```java
@Repository
public interface ExampleRepository extends MongoRepository<Example, String> {
    List<Example> findByStatus(Status status);
    Optional<Example> findByServiceName(String serviceName);
    long countByPriorityAndStatus(Priority priority, Status status);
}
```

#### 4. Exception Handling

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("error", ex.getMessage()));
    }
}
```

### Frontend Patterns

#### 1. Service Pattern

```typescript
@Injectable({ providedIn: 'root' })
export class ApiService {
  private baseUrl = 'http://localhost:8080/api';
  
  constructor(private http: HttpClient) {}
  
  getExample(): Observable<Example[]> {
    return this.http.get<Example[]>(`${this.baseUrl}/example`);
  }
  
  createExample(example: Example): Observable<Example> {
    return this.http.post<Example>(`${this.baseUrl}/example`, example);
  }
}
```

#### 2. Component Pattern

```typescript
@Component({
  selector: 'app-example',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './example.component.html',
  styleUrls: ['./example.component.css']
})
export class ExampleComponent implements OnInit {
  data: any[] = [];
  loading = true;
  
  constructor(private apiService: ApiService) {}
  
  ngOnInit(): void {
    this.loadData();
  }
  
  loadData(): void {
    this.apiService.getExample().subscribe({
      next: (data) => {
        this.data = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading data:', err);
        this.loading = false;
      }
    });
  }
}
```

## 🧪 Testing Guidelines

### Backend Testing

#### Unit Test Pattern

```java
@ExtendWith(MockitoExtension.class)
class ExampleServiceTest {
    
    @Mock
    private ExampleRepository repository;
    
    @InjectMocks
    private ExampleService exampleService;
    
    @Test
    void shouldFindAllExamples() {
        // Given
        List<Example> expected = Arrays.asList(new Example(), new Example());
        when(repository.findAll()).thenReturn(expected);
        
        // When
        List<Example> result = exampleService.findAll();
        
        // Then
        assertEquals(expected, result);
        verify(repository).findAll();
    }
}
```

### Frontend Testing

#### Component Test Pattern

```typescript
describe('ExampleComponent', () => {
  let component: ExampleComponent;
  let fixture: ComponentFixture<ExampleComponent>;
  let apiServiceSpy: jasmine.SpyObj<ApiService>;
  
  beforeEach(() => {
    const spy = jasmine.createSpyObj('ApiService', ['getExample']);
    TestBed.configureTestingModule({
      imports: [ExampleComponent],
      providers: [
        { provide: ApiService, useValue: spy }
      ]
    });
    fixture = TestBed.createComponent(ExampleComponent);
    component = fixture.componentInstance;
    apiServiceSpy = TestBed.inject(ApiService) as jasmine.SpyObj<ApiService>;
  });
  
  it('should load data on init', () => {
    const expectedData = [ { id: '1' } ];
    apiServiceSpy.getExample.and.returnValue(of(expectedData));
    
    component.ngOnInit();
    
    expect(component.data).toEqual(expectedData);
  });
});
```

## 🔍 Code Navigation Tips

### Finding Related Code

**To find where a feature is implemented:**

1. **Start with the Controller** - REST endpoints define the API surface
2. **Follow to Service** - Business logic lives in service classes
3. **Check Repository** - Database operations in repository interfaces
4. **Review Models** - Domain entities define the data structure

**Example: Finding how remediation items are created**

1. Start: `RemediationController.java`
2. Follow: `RemediationService.java`
3. Database: `RemediationItemRepository.java`
4. Model: `RemediationItem.java`

### Understanding Data Flow

**For scanner report processing:**

1. **Upload**: `ReportController.uploadReport()` or `GitHubActionsIntegrationController.ingestScanFromGitHubActions()`
2. **Parsing**: `SecurityReportParser` implementations
3. **Normalization**: `FindingNormalizer`
4. **Deduplication**: `DeduplicationEngine`
5. **Prioritization**: `SecurityPrioritizationEngine`
6. **Storage**: Repository saves to MongoDB
7. **Dashboard**: `DashboardService` aggregates for display

## 🚀 Adding New Features

### Step-by-Step Feature Addition

#### 1. Add New Scanner Support

**Backend:**
1. Create new parser: `NewScannerReportParser.java` implementing `SecurityReportParser`
2. Add to `ReportParserFactory`
3. Update `Tool` enum if needed
4. Add tests for the parser

**Frontend:**
1. Update upload form to support new scanner type
2. Update display logic to handle scanner-specific data

#### 2. Add New API Endpoint

**Backend:**
1. Add method to appropriate `*Controller.java`
2. Implement business logic in `*Service.java`
3. Add repository method if needed
4. Add unit tests
5. Update API documentation

**Frontend:**
1. Add method to `api.service.ts`
2. Create/update component to use new endpoint
3. Add error handling
4. Update routing if needed

#### 3. Add New Dashboard Widget

**Frontend:**
1. Add data fetching to `DashboardService`
2. Create backend endpoint if needed
3. Add widget component
4. Integrate into dashboard HTML
5. Add styling

## ⚠️ Common Pitfalls

### Backend Issues

1. **Missing @CrossOrigin**: Causes CORS errors in frontend
   - **Solution**: Always add `@CrossOrigin(origins = "*")` to controllers

2. **Null Pointer Exceptions**: Missing null checks
   - **Solution**: Use `Optional<>` and proper null checking

3. **MongoDB Connection Issues**: Wrong connection string
   - **Solution**: Verify `MONGODB_URI` in `.env` file

4. **Date Parsing Issues**: Different date formats
   - **Solution**: Use consistent `LocalDateTime` and Jackson configuration

### Frontend Issues

1. **Memory Leaks**: Unsubscribed observables
   - **Solution**: Use `takeUntil()` or `async` pipe

2. **Type Errors**: Missing type definitions
   - **Solution**: Always define TypeScript interfaces/models

3. **CORS Issues**: Frontend can't reach backend
   - **Solution**: Ensure backend has `@CrossOrigin` and correct URLs

4. **Change Detection**: Performance issues
   - **Solution**: Use `OnPush` change detection strategy

## 📊 Performance Considerations

### Backend Optimization

1. **Database Indexing**: Ensure MongoDB indexes on frequently queried fields
2. **Pagination**: Use pagination for large datasets
3. **Caching**: Consider caching expensive operations
4. **Async Processing**: Use `@Async` for long-running operations

### Frontend Optimization

1. **Lazy Loading**: Load components on demand
2. **Virtual Scrolling**: For large lists
3. **Debouncing**: For search inputs
4. **Memoization**: Cache computed values

## 🔐 Security Considerations

### Authentication & Authorization

1. **API Tokens**: Use `SCAN_INGESTION_TOKEN` for external integrations
2. **Environment Variables**: Never commit secrets to code
3. **Input Validation**: Validate all user inputs
4. **SQL Injection**: Use parameterized queries (MongoDB handles this)

### Data Protection

1. **Sensitive Data**: Be careful with logging sensitive information
2. **API Keys**: Rotate API keys regularly
3. **HTTPS**: Use HTTPS in production
4. **Rate Limiting**: Implement rate limiting for public endpoints

## 🛠️ Development Workflow

### Local Development Setup

1. **Start MongoDB**: `docker compose up -d mongodb`
2. **Configure Environment**: Copy `.env.example` to `.env`
3. **Start Backend**: `cd backend && mvn spring-boot:run`
4. **Start Frontend**: `cd frontend && npm start`
5. **Seed Data**: `curl -X POST http://localhost:8080/api/dev/seed`

### Testing Changes

1. **Backend Tests**: `mvn test`
2. **Frontend Tests**: `npm test`
3. **Integration Tests**: Test API endpoints with curl or Postman
4. **E2E Tests**: Test full user flows in the application

### Debugging Tips

**Backend:**
- Use IDE debugger for Java code
- Check logs in console
- Use `System.out.println()` for quick debugging
- Check MongoDB data using MongoDB Compass

**Frontend:**
- Use browser DevTools (F12)
- Check Network tab for API calls
- Use console.log for debugging
- Check Angular DevTools extension

## 📝 Code Style Guidelines

### Java Code Style

- Follow Spring Boot conventions
- Use dependency injection (constructor injection)
- Use meaningful variable names
- Add Javadoc comments for public methods
- Keep methods small and focused

### TypeScript Code Style

- Follow Angular style guide
- Use TypeScript strict mode
- Use meaningful variable names
- Add comments for complex logic
- Keep components small and focused

## 🔄 Continuous Integration

### GitHub Actions Integration

The platform includes GitHub Actions integration for automated security scanning. See `GITHUB_SETUP.md` for details on setting up automated scans in CI/CD pipelines.

### CI/CD Best Practices

1. **Automated Testing**: Run tests on every PR
2. **Security Scanning**: Include Trivy scans in pipeline
3. **Code Quality**: Use linting tools
4. **Deployment**: Automate deployment process

## 📚 Additional Resources

### Internal Documentation

- `README.md` - Main project documentation
- `BUSINESS_GOALS.md` - Business requirements and objectives
- `TRIVY_INTEGRATION_GUIDE.md` - Trivy scanner integration
- `GITHUB_SETUP.md` - GitHub Actions setup
- `QUICK_START.md` - Quick start guide

### External Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Angular Documentation](https://angular.io/docs)
- [MongoDB Documentation](https://docs.mongodb.com/)
- [Gemini API Documentation](https://ai.google.dev/)

## 🤝 Contributing Guidelines

### Making Changes

1. **Understand the context**: Read this guide and relevant documentation
2. **Create a branch**: Use descriptive branch names
3. **Make changes**: Follow existing patterns and conventions
4. **Test thoroughly**: Write and run tests
5. **Document changes**: Update relevant documentation
6. **Submit PR**: Include clear description of changes

### Code Review Checklist

- [ ] Code follows existing patterns
- [ ] Tests are included and passing
- [ ] Documentation is updated
- [ ] No hardcoded values
- [ ] Error handling is appropriate
- [ ] Security implications are considered
- [ ] Performance impact is assessed

This guide provides the essential context for AI models and developers to work effectively with the Security Intelligence Platform codebase. Refer to specific sections based on the task at hand, and always prioritize understanding the existing patterns before introducing new ones.