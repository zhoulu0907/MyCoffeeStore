# 测试运行指南 - Spring AI Alibaba 集成项目

## 快速开始

### 环境要求

#### 后端环境
- Java 17+
- Maven 3.6+
- Spring Boot 3.1.5

#### 前端环境
- Node.js 18+
- npm 或 yarn
- React 18+

#### E2E 环境
- Node.js 18+
- Playwright

## 运行测试

### 1. 后端测试

#### 单元测试
```bash
# 进入后端目录
cd /Users/kanten/MyCoffeeStore/backend

# 运行所有单元测试
mvn test

# 运行特定测试类
mvn test -Dtest=ModelScopeChatModelTest

# 运行特定测试方法
mvn test -Dtest=ModelScopeChatModelTest#testChatStream_Success

# 生成测试报告
mvn surefire-report:report
```

#### 集成测试
```bash
# 运行所有集成测试
mvn verify

# 运行特定集成测试类
mvn test -Dtest=LlmConfigIntegrationTest

# 使用 Spring Boot Test 运行
mvn spring-boot:test-run
```

#### 测试覆盖率
```bash
# 生成 JaCoCo 报告
mvn jacoco:report

# 查看覆盖率报告
open target/site/jacoco/index.html
```

### 2. 前端测试

#### 安装依赖
```bash
# 进入前端目录
cd /Users/kanten/MyCoffeeStore/frontend/my-coffee-store-frontend

# 安装测试依赖
npm install --save-dev jest @testing-library/react @testing-library/jest-dom
```

#### 运行测试
```bash
# 运行所有测试
npm test

# 运行测试并生成覆盖率报告
npm run test:coverage

# 监视模式运行测试
npm run test:watch

# 运行特定测试文件
npm test -- --testPathPattern=LlmConfigSection

# 运行特定测试用例
npm test -- --testNamePattern="renders LLM configuration form"
```

#### 测试配置文件
- `jest.config.js` - Jest 配置
- `setupTests.ts` - 测试环境设置

### 3. 端到端测试

#### 安装依赖
```bash
# 安装 Playwright
npm install -D @playwright/test

# 安装浏览器
npx playwright install
```

#### 运行测试
```bash
# 运行所有 E2E 测试
npx playwright test

# 运行特定测试文件
npx playwright test admin-flow.spec.ts

# 运行测试并生成报告
npx playwright test --reporter=html

# 运行测试并查看视频
npx playwright test --headed --reporter=list

# 运行测试并生成覆盖率报告
npx playwright test --coverage
```

#### 测试环境配置
- `.env.test` - 测试环境变量
- `playwright.config.ts` - Playwright 配置

## 测试环境配置

### 后端配置

#### application-test.yml
```yaml
server:
  port: 8081

spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: password

  h2:
    console:
      enabled: true

# 测试专用的配置
modelscope:
  api-key: test-api-key
  base-url: https://api.test.cn/v1
  model: test-model
  timeout: 30000
```

#### 测试数据初始化
```java
@TestConfiguration
public class TestDataSourceConfig {
    @Bean
    public DataSource dataSource() {
        return DataSourceBuilder.create()
                .url("jdbc:h2:mem:testdb")
                .driverClassName("org.h2.Driver")
                .username("sa")
                .password("")
                .build();
    }
}
```

### 前端配置

#### 测试环境变量
```env
# .env.test
REACT_APP_API_URL=http://localhost:8081
REACT_APP_ADMIN=true
```

#### 测试 Mock
```javascript
// src/__mocks__/services/api.js
export const mockAdminApi = {
  getLlmConfigs: jest.fn(),
  updateLlmConfig: jest.fn(),
  testLlmConnection: jest.fn(),
  getUsers: jest.fn(),
  updateUserStatus: jest.fn(),
};
```

### E2E 配置

#### 测试环境变量
```env
# .env.test
BASE_URL=http://localhost:3000
API_URL=http://localhost:8081
ADMIN_USERNAME=admin
ADMIN_PASSWORD=admin123
```

## 测试数据管理

### 后端测试数据

#### 使用 @TestPropertySource
```java
@TestPropertySource(properties = {
    "modelscope.api-key=test-api-key",
    "modelscope.base-url=https://api.test.cn/v1"
})
class ModelScopeChatModelTest {
    // 测试代码
}
```

#### 使用测试数据集
```java
@Sql(scripts = "/test-data.sql")
@DataJpaTest
class UserRepositoryTest {
    // 测试代码
}
```

### 前端测试数据

#### 使用 fixtures
```javascript
// tests/fixtures/users.js
export const mockUsers = [
  {
    id: 1,
    username: 'test_user',
    email: 'test@example.com',
    balance: 100.00,
    orderCount: 10,
    status: 'active'
  }
];
```

#### 使用 factories
```javascript
// tests/factories/userFactory.js
export const createUser = (overrides) => ({
  id: 1,
  username: 'test_user',
  email: 'test@example.com',
  balance: 100.00,
  orderCount: 10,
  status: 'active',
  ...overrides
});
```

## CI/CD 集成

### GitHub Actions 配置

#### 后端测试
```yaml
# .github/workflows/backend-test.yml
name: Backend Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Cache Maven packages
        uses: actions/cache@v3
        with:
          path: ~/.m2
          key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
          restore-keys: ${{ runner.os }}-m2
      - name: Run tests
        run: mvn clean test
      - name: Generate coverage report
        run: mvn jacoco:report
      - name: Upload coverage to Codecov
        uses: codecov/codecov-action@v3
```

#### 前端测试
```yaml
# .github/workflows/frontend-test.yml
name: Frontend Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'
      - name: Cache npm
        uses: actions/cache@v3
        with:
          path: ~/.npm
          key: ${{ runner.os }}-node-${{ hashFiles('**/package-lock.json') }}
          restore-keys: ${{ runner.os }}-node
      - name: Install dependencies
        run: npm ci
      - name: Run tests
        run: npm run test:coverage
      - name: Upload coverage
        uses: codecov/codecov-action@v3
```

#### E2E 测试
```yaml
# .github/workflows/e2e-test.yml
name: E2E Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    services:
      app:
        image: myapp:latest
        ports:
          - 8081:8081
        options: >-
          --health-cmd "curl -f http://localhost:8081/actuator/health || exit 1"
          --health-interval 30s
          --health-timeout 10s
          --health-retries 3
    steps:
      - uses: actions/checkout@v3
      - name: Set up Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'
      - name: Install dependencies
        run: npm ci
      - name: Install Playwright browsers
        run: npx playwright install
      - name: Run E2E tests
        run: npx playwright test
```

## 常见问题

### 1. 测试失败

#### 后端测试失败
```bash
# 查看详细错误信息
mvn test -X

# 运行单个测试并查看输出
mvn test -Dtest=ModelScopeChatModelTest#testChatStream_Success -DforkCount=0

# 使用 Spring Boot Test 调试
mvn spring-boot:test-run -Dspring-boot.test.args="--debug"
```

#### 前端测试失败
```bash
# 查看详细错误信息
npm test -- --verbose

# 使用调试模式
npm test -- --runInBand

# 使用 React Testing Library 的调试工具
import { screen } from '@testing-library/react';
screen.logTestingPlaygroundURL();
```

#### E2E 测试失败
```bash
# 使用 headed 模式查看 UI
npx playwright test --headed

# 使用调试模式
npx playwright test --debug

# 查看测试日志
npx playwright test --reporter=list
```

### 2. 性能问题

#### 后端测试慢
```bash
# 并行运行测试
mvn test -Dthreads=4

# 使用 Spring Boot Test 的并行配置
mvn spring-boot:test-run -Dspring-boot.test.parallel=true
```

#### 前端测试慢
```bash
# 使用 jest 的并行运行
npm test -- --maxWorkers=4

# 使用缓存
npm test -- --cache
```

### 3. 依赖问题

#### Maven 依赖问题
```bash
# 清理并重新下载依赖
mvn clean dependency:purge-local-repository

# 更新依赖版本
mvn versions:display-dependency-updates
```

#### npm 依赖问题
```bash
# 清理并重新安装
rm -rf node_modules package-lock.json
npm install

# 使用 npm ci 替代 npm install
npm ci
```

## 最佳实践

### 1. 测试命名

#### 后端
```java
// 好的命名
@Test
void should_ReturnValidResponse_When_CallingChatStream_WithValidMessages() {
    // 测试代码
}

// 不好的命名
@Test
void test1() {
    // 测试代码
}
```

#### 前端
```javascript
// 好的命名
test('renders LLM configuration form with default values', () => {
  // 测试代码
});

// 不好的命名
test('test component', () => {
  // 测试代码
});
```

### 2. 断言原则

#### 使用明确的断言
```java
// 好的断言
assertEquals("expected", actual);
assertTrue(condition, "Error message");
assertNotNull(object, "Object should not be null");

// 不好的断言
assert(true); // 没有具体信息
```

### 3. Mock 使用

#### 合理使用 Mock
```java
// 好的 Mock
@Mock
private WebClient mockWebClient;

@BeforeEach
void setUp() {
    when(mockWebClient.post()).thenReturn(mockRequestBuilder);
}

// 不好的 Mock（过度 Mock）
@Mock
private SomeDependency someDependency;
```

### 4. 测试隔离

#### 每个测试独立
```java
@Test
void should_SaveConfig_WithValidData() {
    // 清理或准备数据
    // 执行测试
    // 验证结果
    // 清理数据
}
```

## 调试技巧

### 1. 后端调试

#### 使用调试器
```java
// 设置断点
@SneakyThrows
private void someMethod() {
    // 在这里设置断点
    Thread.sleep(1000);
}
```

#### 使用日志
```java
@Slf4j
class TestClass {
    @Test
    void debugTest() {
        log.info("Starting test");
        // 测试代码
        log.info("Test completed");
    }
}
```

### 2. 前端调试

#### 使用 React Testing Library 的调试工具
```javascript
import { render, screen } from '@testing-library/react';

test('debug component', () => {
  render(<MyComponent />);
  screen.debug(); // 打印 DOM 结构
});
```

#### 使用 JSDOM 调试
```javascript
const { JSDOM } = require('jsdom');

test('debug dom', () => {
  const dom = new JSDOM(`<!DOCTYPE html><html><body></body></html>`);
  global.window = dom.window;
  // 测试代码
});
```

### 3. E2E 调试

#### 使用 Playwright 的调试模式
```javascript
test('debug e2e', async ({ page }) => {
  await page.goto('/');
  await page.pause(); // 暂停测试，手动调试
});
```

## 总结

本指南提供了完整的测试运行方法，包括环境配置、测试执行、问题调试等方面。按照本指南执行，可以确保测试的顺利进行和高质量的测试结果。

如有任何问题，请参考项目的 issue 系统或联系开发团队。