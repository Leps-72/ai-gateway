# Nhật ký sử dụng AI trong dự án AI Gateway

## 1. Tổng quan dự án

AI Gateway là một REST API backend đóng vai trò lớp trung gian tập trung giữa các ứng dụng client và nhà cung cấp AI. Thay vì gọi trực tiếp Gemini, client gửi yêu cầu đến gateway; gateway chịu trách nhiệm xác thực, gọi provider, lưu lịch sử request/response, thu thập usage metrics và xử lý các vấn đề reliability.

Project được xây dựng bằng Java 17, Spring Boot và Maven. Authentication sử dụng Spring Security, JWT và BCrypt. Dữ liệu được quản lý qua Spring Data JPA/Hibernate, với H2 file database cho local development và Aiven Managed MySQL cho môi trường production. Gemini là AI provider hiện tại, được truy cập thông qua abstraction `AiProvider`. Hệ thống còn có structured output, retry, timeout, rate limiting, logging, global error handling, Swagger/OpenAPI, Docker và cấu hình triển khai trên Render.

AI được sử dụng như một công cụ hỗ trợ phân tích, viết code, kiểm thử và review. Các quyết định về phạm vi, kiến trúc, bảo mật và deployment vẫn do người phát triển xác nhận. Mọi đề xuất quan trọng đều được đối chiếu lại với source code và kết quả chạy thực tế.

## 2. Công cụ AI đã sử dụng

| Công cụ | Vai trò trong dự án |
|---|---|
| ChatGPT | Hỗ trợ chia nhỏ challenge thành từng bước, làm rõ yêu cầu, thảo luận kiến trúc, xác định phạm vi và review hướng triển khai. |
| Codex | Đọc và chỉnh sửa source code trong workspace, chạy test/build, debug lỗi, kiểm tra cấu hình, thực hiện audit dựa trên source code và xác minh thay đổi. |

Hai công cụ không được xem là nguồn sự thật tuyệt đối. Source code hiện tại, test results, runtime behavior và cấu hình deployment mới là source of truth của project.

## 3. AI đã hỗ trợ quá trình phát triển như thế nào

### 3.1. Khởi tạo backend và API cơ bản

AI hỗ trợ tạo Spring Boot project tối thiểu với Java 17, Maven và Spring Web, sau đó xây dựng `GET /health`. Các bước tiếp theo bổ sung `POST /ai/chat`, validation đầu vào, DTO và cách trả HTTP status phù hợp. Cấu trúc ban đầu được giữ đơn giản để dễ hiểu và dễ giải thích ở cấp độ Backend Intern.

### 3.2. Database và lưu trữ request

AI hỗ trợ tích hợp Spring Data JPA và H2, xây dựng entity `Conversation`, repository, service và `GET /conversations`. Schema sau đó được mở rộng để lưu response, provider, model, input/output tokens, latency, status và `userId`. Usage metrics được tính từ dữ liệu đã lưu thay vì gọi lại provider.

Khi chuẩn bị deployment, AI hỗ trợ audit khả năng tương thích H2 với MySQL, phân biệt thay đổi thuộc code, configuration, dependency và schema. Local development tiếp tục dùng H2, trong khi production sử dụng Aiven Managed MySQL thông qua Spring profile và environment variables.

### 3.3. Authentication và security

AI hỗ trợ triển khai register/login, BCrypt password hashing, JWT generation/validation, `JwtAuthenticationFilter` và stateless Spring Security. Các endpoint public gồm health, authentication và Swagger/OpenAPI; các endpoint AI, conversations và usage tiếp tục yêu cầu Bearer token.

Trong quá trình review, AI cũng giúp phát hiện Spring Boot vẫn tự tạo generated security password và `inMemoryUserDetailsManager`. Phần auto-configuration thừa được loại bỏ mà không thay đổi authentication flow tùy chỉnh. `spring.jpa.open-in-view=false` được sử dụng vì project là REST API và không cần Open Session in View.

### 3.4. Gemini và provider abstraction

AI hỗ trợ tích hợp Google GenAI Java SDK, đọc `GEMINI_API_KEY` từ environment variable và không hard-code secret. Chức năng chat trả text thực tế từ Gemini, còn analyze yêu cầu structured output gồm `summary`, `sentiment`, `category` và `priority`, sau đó parse và validate thành Java DTO thay vì trả raw output.

Sau đó, `AiProvider` được tạo để controller không phụ thuộc trực tiếp vào Gemini-specific implementation. `GeminiService` là implementation hiện tại, còn `AiGatewayService` đảm nhiệm orchestration và lưu metrics. Model được chuyển sang configuration, với default `gemini-3.5-flash-lite` và có thể override bằng `GEMINI_MODEL`. Thiết kế này cho phép bổ sung provider khác trong tương lai mà không cần thay đổi API contract của controller.

### 3.5. Reliability, observability và error handling

AI hỗ trợ thêm timeout khoảng 10 giây, tối đa 3 attempts với backoff đơn giản và chỉ retry các lỗi có khả năng tạm thời như 429, 5xx, timeout hoặc network transient error. Validation error, authentication error, duplicate username, provider error, timeout và malformed structured output được chuyển thành `ErrorResponse` thống nhất thông qua global exception handling.

Retry diễn ra bên trong provider call nên một request cuối cùng chỉ tạo một `Conversation` record. `latencyMs` phản ánh tổng thời gian client trải nghiệm, bao gồm retry và backoff. Project cũng có rate limiting và logging nhưng không ghi API key, JWT, password hoặc sensitive headers.

### 3.6. Documentation, frontend và deployment

AI hỗ trợ cấu hình Swagger/OpenAPI, JWT Bearer scheme và Postman collection. Frontend management console được xây dựng bằng React/Vite cho authentication, protected routes, health status, Dashboard và Usage sử dụng dữ liệu backend thật. Frontend không chứa secret và không fake runtime metrics.

Đối với deployment, AI hỗ trợ kiểm tra environment variables, `PORT`, production profile, secret handling và tạo Docker multi-stage build. Backend đã được triển khai thành công trên Render, kết nối Aiven MySQL; health endpoint và Swagger hoạt động trên môi trường deploy. Tài liệu này không ghi hostname, username, password, API key hoặc JWT secret thực tế.

## 4. Các sai sót quan trọng của AI và cách khắc phục

### 4.1. Hướng Physical AI ban đầu không còn phù hợp

Project ban đầu được định hướng là Physical AI Gateway, với output dạng robot action như `move`, `pickup`, `drop` và `wait`. Sau khi đối chiếu lại challenge, hướng này quá đặc thù và không phản ánh mục tiêu generic AI Gateway.

Người phát triển quyết định refactor sang gateway tổng quát. Robot-specific prompt và `RobotAction` được loại khỏi `/ai/chat`; response mới gồm text, provider, model và status. Package/project cũng được đổi từ tên liên quan `physicalai` sang `com.aigateway`. Bài học là cần xác nhận mục tiêu sản phẩm sớm và tránh mở rộng theo một use case chưa chắc thuộc yêu cầu cuối cùng.

### 4.2. Environment variable và rủi ro lộ secret

Khi cấu hình `GEMINI_API_KEY` trên Windows, process Codex đang chạy không nhận được biến môi trường mới cho đến khi ứng dụng được khởi động lại. Trong quá trình chẩn đoán, một thao tác đã vô tình echo giá trị key. Không có cơ sở để ghi rằng key đã được rotate chỉ từ hành động của AI; việc quản lý và rotate secret thuộc trách nhiệm của người phát triển.

Sau sự cố, quy trình được siết chặt: chỉ kiểm tra key có tồn tại hay không, không in giá trị, không truyền key trực tiếp trên command line và không dùng công cụ có khả năng echo environment variable. Bài học chính là chẩn đoán secret phải dựa trên boolean/presence check và luôn giả định terminal output có thể được lưu lại.

### 4.3. Gemini trả 429 do quota

Gemini từng trả `429 RESOURCE_EXHAUSTED` vì daily quota. Đây không phải lỗi build hoặc lỗi gateway. Việc tiếp tục gọi API nhiều lần chỉ làm tình hình xấu hơn và không cung cấp thêm thông tin hữu ích.

Các bước sau đó ưu tiên unit test/mock và test những phần không gọi Gemini. Model được cấu hình qua `GEMINI_MODEL`, default là `gemini-3.5-flash-lite`. Retry nhận diện 429 là provider/rate-limit error nhưng vẫn giới hạn tối đa 3 attempts. Không có pricing nào được tự suy đoán cho model mới; nếu thiếu configuration thì cost estimation được đánh dấu unavailable.

### 4.4. Data isolation ban đầu chưa đúng

Phiên bản ban đầu của `GET /conversations` và `GET /usage` tính toán trên toàn bộ dữ liệu, khiến user đã xác thực có thể nhìn thấy hoặc tổng hợp request của user khác. Đây là vấn đề isolation thực sự, không chỉ là cải tiến kiến trúc.

Implementation được sửa để scope query theo `userId` lấy từ authenticated principal. `Conversation` có index cho `userId` để hỗ trợ truy vấn. Quan hệ hiện tại vẫn là scalar logical reference bằng `Long userId`, không phải JPA association và không có physical foreign key. Cách này đủ nhỏ cho prototype nhưng limitation phải được ghi nhận: referential integrity được đảm bảo ở application layer thay vì database.

### 4.5. Lỗi dependency injection của `GeminiService`

Application từng không startup vì Spring cố gọi constructor rỗng của `GeminiService`. Nguyên nhân là class có nhiều constructor phục vụ production và test nhưng constructor production không được đánh dấu rõ cho dependency injection, khiến Spring không xác định được constructor cần dùng và tìm default constructor không tồn tại.

Thay vì thêm constructor rỗng và làm dependency trở thành optional, constructor production được đánh dấu `@Autowired` theo constructor injection chuẩn. Regression test được bổ sung để bảo vệ cấu hình này. Kết quả cuối cùng là 36 tests pass và Maven package thành công.

### 4.6. Spring Security auto-configuration thừa

Dù project đã có JWT authentication riêng, startup vẫn hiển thị generated security password và thông báo về `inMemoryUserDetailsManager`. Nguyên nhân là default `UserDetailsServiceAutoConfiguration` vẫn hoạt động; custom JWT filter không tự động vô hiệu hóa toàn bộ phần auto-configuration này.

Project đã loại trừ auto-configuration thừa theo cách có chủ đích, thay vì chỉ ẩn log. Authentication vẫn dùng `AuthService`, BCrypt, `JwtService`, `JwtAuthenticationFilter` và `SecurityContext`. Các public/protected endpoint không thay đổi. Warning Open Session in View cũng được xử lý bằng `spring.jpa.open-in-view=false`, còn Swagger/OpenAPI được giữ vì cần cho recruitment demo.

### 4.7. Xác minh deployment bằng hành vi thực tế

AI có thể đề xuất Dockerfile và Render configuration, nhưng trạng thái deployment không nên được kết luận chỉ từ file cấu hình. Việc xác minh được thực hiện bằng build container, startup, health check, Swagger và kết nối database thực tế.

Backend đã deploy thành công trên Render bằng Docker multi-stage build và sử dụng Aiven Managed MySQL cho production. Register, restart và login lại xác nhận persistence của MySQL. Báo cáo chỉ ghi nhận kết quả, không đưa secret, hostname database hoặc credentials vào repository.

## 5. Cách xác minh kết quả

Các thay đổi được xác minh bằng nhiều lớp thay vì chỉ dựa vào code generation:

- Chạy toàn bộ Maven tests và kiểm tra regression sau các thay đổi quan trọng.
- Chạy clean package để xác nhận JAR được build thành công.
- Startup application với cấu hình phù hợp nhưng không tự động gọi Gemini.
- Kiểm tra `GET /health`, Swagger UI và OpenAPI JSON.
- Kiểm tra register/login, BCrypt hashing, JWT hợp lệ, JWT sai và protected endpoints.
- Kiểm tra database persistence qua restart và kiểm tra isolation theo authenticated `userId`.
- Kiểm tra Docker build thông qua quá trình deployment trên Render, sau đó xác minh application startup, health check và Swagger trên môi trường deploy.
- Audit source code cho entity, repository, security flow, provider flow và environment configuration.
- Quét repository để tránh commit secret, token, credential, log, H2 data hoặc build artifacts.

Kết quả test cuối được ghi nhận là **36 tests pass, 0 failures, 0 errors, 0 skipped**; clean package trả về **BUILD SUCCESS**. Gemini không được gọi liên tục trong quá trình test, đặc biệt khi quota đã hết. Các phần provider được kiểm thử bằng mock hoặc bằng những test không thực hiện network request thật.

## 6. Các quyết định do con người đưa ra

- Chuyển sản phẩm từ Physical AI sang generic AI Gateway.
- Giữ kiến trúc monolith Spring Boot thay vì thêm microservice hoặc queue.
- Chọn JWT stateless và BCrypt cho authentication prototype.
- Giữ H2 cho local, Aiven MySQL cho production và dùng Spring profile/environment variables.
- Yêu cầu mọi protected data được scope theo authenticated user.
- Chọn provider abstraction nhưng chưa thêm provider thứ hai.
- Không spam Gemini khi gặp quota 429; ưu tiên mock và offline verification.
- Giữ Swagger public cho recruitment demo.
- Chọn Docker để deploy backend Java lên Render.
- Hoãn các refactor lớn không cần thiết trước submission.

## 7. Hạn chế hiện tại

- Chỉ có một AI provider thực tế là Gemini; chưa có routing hoặc fallback.
- `Conversation` hiện biểu diễn một AI request/response kèm metrics, chưa phải conversation session nhiều message.
- `userId` là logical reference, chưa có physical foreign key hoặc JPA relationship.
- Chưa có refresh token, role/permission system, password reset hoặc email verification.
- Retry và timeout là cấu hình đơn giản; chưa có circuit breaker.
- Usage chỉ là aggregate từ dữ liệu đã lưu, chưa có time-series analytics.
- Cost estimation phụ thuộc vào pricing configuration và token metadata; thiếu dữ liệu thì kết quả unavailable.
- Rate limiting phù hợp prototype nhưng chưa được thiết kế cho nhiều instance phân tán.
- Schema management hiện còn đơn giản; production lâu dài nên dùng migration tool.

## 8. Nếu có thêm 7 ngày

- Thêm Flyway hoặc Liquibase để quản lý schema migration rõ ràng.
- Cân nhắc physical foreign key từ `Conversation.userId` đến `users.id` sau khi lập migration an toàn.
- Bổ sung provider thứ hai thông qua `AiProvider`, sau đó triển khai routing/fallback có kiểm soát.
- Thêm circuit breaker và metrics/alerting cho lỗi provider.
- Hoàn thiện real conversation model nếu product cần multi-turn chat.
- Thêm integration test với Testcontainers MySQL để giảm khác biệt H2/MySQL.
- Cải thiện rate limiting cho môi trường nhiều instance bằng shared store nếu deployment yêu cầu.
- Thêm CI pipeline cho test, package, secret scan và Docker build.
- Hoàn thiện deployment documentation và operational runbook.
- Hoàn thiện frontend management console nếu cần một giao diện quản trị đầy đủ cho demo và vận hành.

## 9. Bài học chính

Giá trị lớn nhất của AI trong project này là tăng tốc vòng lặp phân tích–triển khai–kiểm thử, đặc biệt khi chia challenge thành các bước nhỏ và khi audit source code. Tuy nhiên, AI cũng có thể đi sai hướng, suy luận quá mức hoặc đưa ra thao tác chẩn đoán không an toàn. Vì vậy, cách làm hiệu quả nhất là giữ source code và kết quả chạy làm source of truth, giới hạn phạm vi từng bước, review mọi thay đổi liên quan security/database/deployment và luôn có kiểm thử xác nhận.

Project cho thấy AI có thể hỗ trợ một Backend Intern xây dựng hệ thống đầy đủ hơn trong thời gian ngắn, nhưng trách nhiệm kỹ thuật cuối cùng vẫn thuộc về người phát triển: chọn kiến trúc phù hợp, bảo vệ secret, xác minh behavior và biết rõ những limitation đang được chấp nhận.
