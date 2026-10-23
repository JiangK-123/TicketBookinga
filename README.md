# 星河游乐场订票

前后端分开构建、分开启动。浏览器里的 Vue 页面通过 Axios 直接请求 `http://localhost:8080`，跨域由 Spring 的 CORS 配置放行，不走开发服务器代理。

## 目录

- `backend`：Spring Boot 3.5，`@RestController` 提供 JSON 接口，内存保存项目和订单
- `frontend`：Vue 3 + Vue Router + Axios
- `docs/openapi.json`：从运行中的服务导出的 OpenAPI，字段已与接口测试对照

## 环境

- JDK 17 或更高（本机用 JDK 25 验证过）
- Maven 3.6.3+
- Node.js 20+

若系统里没有 `mvn` / `npm`，可使用仓库里的 `.tools`（已包含 Maven 3.9.11 与 Node 22.14）。启动脚本会优先用系统命令，找不到再回退到 `.tools`。

## 分别构建

```powershell
cd backend
mvn package

cd ..\frontend
npm install
npm run build
```

后端产物是 `backend/target/park-ticket-1.0.0.jar`。前端产物在 `frontend/dist`。

## 分别启动

开两个终端。

```powershell
# 终端 1：后端，端口 8080
java -jar backend\target\park-ticket-1.0.0.jar

# 终端 2：前端开发服务，端口 5173
cd frontend
npm run dev
```

也可以用脚本：

```powershell
.\scripts\run-backend.ps1
.\scripts\run-frontend.ps1
```

用构建产物联调时，前端改为：

```powershell
cd frontend
npm run preview
```

`preview` 监听 `http://localhost:4173`，这个源已经写在后端 CORS 白名单里。

接口文档：<http://localhost:8080/swagger-ui.html>  
OpenAPI JSON：<http://localhost:8080/v3/api-docs>

换机器或换端口时，改两处：

- 前端 `frontend/.env` 的 `VITE_API_BASE_URL`（改完要重新 `npm run dev` 或 `npm run build`）
- 后端 `backend/src/main/resources/application.yml` 的 `app.cors.allowed-origins`

## 页面

1. 首页按分类和名称筛选项目。
2. 项目页选择日期和人数。提交前会查询当日余票；校验失败时，把接口 `details` 显示在对应字段下。
3. 订单页用下单手机号查询。待支付订单可以支付或取消，取消会退回余票。

## 接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/attractions/categories` | 分类编码和中文名 |
| GET | `/api/attractions?category&keyword` | 项目列表。`keyword` 匹配名称 |
| GET | `/api/attractions/{id}` | 项目详情 |
| GET | `/api/attractions/{id}/availability?date=yyyy-MM-dd` | 当日限额、已售、剩余 |
| POST | `/api/orders` | 创建订单，201，`Location` 指向详情 |
| GET | `/api/orders?phone=` | 按手机号精确查询 |
| GET | `/api/orders/{orderNo}` | 订单详情 |
| POST | `/api/orders/{orderNo}/pay` | 支付，仅 `UNPAID` |
| POST | `/api/orders/{orderNo}/cancel` | 取消并退回余票，仅 `UNPAID` |

金额单位是元。日期是 `yyyy-MM-dd`。`createdAt` 是本地时间，没有时区后缀。订单号形如 `PK202609240001`。

订单状态：`UNPAID` 待支付，`PAID` 已支付，`CANCELLED` 已取消。

错误体：

```json
{
  "code": "VALIDATION_ERROR",
  "message": "请求参数不合法",
  "details": [{ "field": "visitorPhone", "message": "手机号须为 1 开头的 11 位数字" }],
  "path": "/api/orders",
  "timestamp": "2026-09-24T18:40:00.000+08:00"
}
```

## 已核对的字段和错误边界

`mvn test` 覆盖了下面这些情况，并与 Vue 提交的字段名一致。

创建订单字段：`attractionId`、`visitDate`、`quantity`、`visitorName`、`visitorPhone`。姓名和手机号会先去掉首尾空格。姓名不能包含数字（含全角数字）。未知 JSON 字段忽略。

| 场景 | HTTP | code |
| --- | --- | --- |
| 缺字段、姓名空白、姓名包含数字、手机号不是 1 开头的 11 位、数量小于 1 或大于 10、姓名超过 20 字 | 400 | `VALIDATION_ERROR` |
| 日期早于今天，或晚于今天之后第 30 天 | 400 | `INVALID_DATE` |
| 今天，以及今天之后第 30 天 | 200 | 允许预订 |
| `date` 缺失 | 400 | `MISSING_PARAMETER` |
| `date` 或 `id` 类型不对 | 400 | `TYPE_MISMATCH` |
| 分类编码未知 | 400 | `INVALID_CATEGORY` |
| JSON 无法解析，或数量不是整数 | 400 | `MALFORMED_JSON` |
| 不是 JSON | 415 | `UNSUPPORTED_MEDIA` |
| 项目不存在、订单不存在、未知 `/api/**` 路径 | 404 | `NOT_FOUND` |
| 余票不足 | 409 | `SOLD_OUT`，消息含当日剩余张数 |
| 重复支付、取消已支付或已取消订单 | 409 | `ILLEGAL_STATE` |
| 查询订单缺少手机号 | 400 | `MISSING_PARAMETER` |
| 方法不允许 | 405 | `METHOD_NOT_ALLOWED` |
| `Origin: http://localhost:5173` 的 GET 和 POST 预检 | 200 | 返回 `Access-Control-Allow-Origin` |
| 其他 Origin | 403 | 不放行 |

深海潜艇的日限额是 20 张，用来演示售罄。取消未支付订单后，这一天的余票会加回去。

## 测试

```powershell
cd backend
mvn test
```
