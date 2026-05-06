# MovieTicket Server

基于 Spring Boot 的电影购票系统后端服务，提供电影管理、影院管理、放映场次管理、票务管理、订单管理及用户/管理员认证等完整 RESTful API。

## 技术栈

| 技术 | 版本 | 说明 |
|---|---|---|
| Java | 1.8 | 编程语言 |
| Spring Boot | 2.7.6 | 应用框架 |
| MyBatis | 2.3.0 | ORM 框架 |
| MySQL | 8.x | 关系型数据库 |
| Redis | - | Token 缓存 / 会话管理 |
| Druid | 1.2.6 | 数据库连接池 |
| PageHelper | 1.4.6 | 分页插件 |
| JWT (jjwt) | 0.11.2 | Token 生成与解析 |
| Hutool | 5.8.21 | Java 工具库 |
| SpringDoc OpenAPI | 1.7.0 | Swagger UI 接口文档 |
| Lombok | - | 简化 POJO 代码 |
| Snowflake | - | 分布式唯一 ID 生成 |

## 项目结构

```
src/main/java/com/shaber/movieticket/
├── config/          # 配置类（Redis、Snowflake、WebMVC 跨域/拦截器）
├── controller/      # 控制器层（REST 接口）
├── service/         # 服务层接口
│   └── impl/        # 服务层实现
├── mapper/          # MyBatis Mapper 接口
├── pojo/            # 实体类
├── dto/             # 数据传输对象（查询返回）
├── vo/              # 视图对象（请求参数）
│   └── pagequery/   # 分页查询 VO
├── resp/            # 统一响应封装
├── filter/          # Token 拦截器
├── exception/       # 自定义业务异常
└── utils/           # 工具类（JWT、MD5、Snowflake 等）

src/main/resources/
├── mapper/          # MyBatis XML 映射文件
├── application.yml  # 应用配置
└── logback.xml      # 日志配置
```

## 快速开始

### 环境要求

- JDK 1.8+
- Maven 3.6+
- MySQL 8.x
- Redis

### 配置

修改 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/cinema?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: root
    password: 123456

  redis:
    host: localhost
    port: 6379
    database: 0

# 电影封面上传目录（绝对路径）
movie:
  cover:
    upload:
      path: /your/upload/path

server:
  port: 8090
```

### 构建与运行

```bash
# 编译打包
mvn clean package -DskipTests

# 运行
java -jar target/movieticket_server-0.0.1-SNAPSHOT.jar
```

服务默认监听 `http://localhost:8090`。

Swagger UI 地址：`http://localhost:8090/swagger-ui.html`

## 认证机制

登录成功后服务返回 Token，格式为：

```
{role}:{uuid}
```

其中 `role` 为 `user` 或 `admin`。后续需要认证的接口在请求头中携带：

```
Authorization: user:xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
```

Token 存储于 Redis，有效期 30 分钟，剩余不足 5 分钟时自动续期，新 Token 通过响应头 `new-token` 返回。

## API 接口

> 所有接口均以 `POST` 方式请求，请求/响应均为 `application/json`。

### 用户模块 `/user`

| 接口 | 认证 | 说明 |
|---|---|---|
| `POST /user/login` | 否 | 用户登录 |
| `POST /user/register` | 否 | 用户注册 |
| `POST /user/list` | 是 | 分页查询用户列表 |
| `POST /user/update` | 是 | 更新用户信息 |
| `POST /user/updatePwd` | 是 | 修改密码 |
| `POST /user/updatePhone` | 是 | 修改手机号 |
| `POST /user/delete` | 是 | 删除用户 |
| `POST /user/basic` | 是 | 获取当前用户基本信息 |
| `POST /user/recharge` | 是 | 账户充值 |

### 管理员模块 `/admin`

| 接口 | 认证 | 说明 |
|---|---|---|
| `POST /admin/login` | 否 | 管理员登录 |
| `POST /admin/register` | 否 | 管理员注册 |
| `POST /admin/list` | 是 | 分页查询管理员列表 |
| `POST /admin/updatePwd` | 是 | 修改密码 |
| `POST /admin/updatePhone` | 是 | 修改手机号 |
| `POST /admin/basic` | 是 | 获取当前管理员基本信息 |

### 电影模块 `/movie`

| 接口 | 认证 | 说明 |
|---|---|---|
| `POST /movie/list` | 否 | 分页查询上映中电影（用户端） |
| `POST /movie/listAdmin` | 否 | 分页查询全部电影（管理端） |
| `POST /movie/listUpCast` | 否 | 查询即将上映的电影 |
| `POST /movie/getCast` | 否 | 分页查询正在上映的电影预告 |
| `POST /movie/upComing` | 否 | 分页查询即将上映电影 |
| `POST /movie/add` | 是 | 新增电影 |
| `POST /movie/update` | 是 | 更新电影信息 |
| `POST /movie/delete` | 是 | 删除电影 |
| `POST /movie/upcast` | 是 | 手动上映电影 |
| `POST /movie/downcast` | 是 | 手动下映电影 |
| `POST /movie/auto` | 是 | 自动根据日期上映/下映 |

### 影院模块 `/cinema`

| 接口 | 认证 | 说明 |
|---|---|---|
| `POST /cinema/list` | 否 | 分页查询影院（用户端） |
| `POST /cinema/listAdmin` | 是 | 分页查询影院（管理端） |
| `POST /cinema/listAll` | 否 | 查询全部影院 |
| `POST /cinema/add` | 是 | 新增影院 |
| `POST /cinema/update` | 是 | 更新影院信息 |
| `POST /cinema/delete` | 是 | 删除影院 |

### 放映厅模块 `/screenroom`

| 接口 | 认证 | 说明 |
|---|---|---|
| `POST /screenroom/list` | 否 | 分页查询放映厅 |
| `POST /screenroom/listAll` | 否 | 查询全部放映厅 |
| `POST /screenroom/listCid` | 否 | 按影院查询放映厅 |
| `POST /screenroom/add` | 是 | 新增放映厅 |
| `POST /screenroom/update` | 是 | 更新放映厅信息 |

### 放映场次模块 `/screening`

| 接口 | 认证 | 说明 |
|---|---|---|
| `POST /screening/list` | 否 | 分页查询放映场次 |
| `POST /screening/listByCid` | 否 | 按放映厅和日期查询场次 |
| `POST /screening/listByMid` | 是 | 按电影查询场次 |
| `POST /screening/add` | 是 | 新增放映场次 |
| `POST /screening/update` | 是 | 更新场次信息 |
| `POST /screening/delete` | 是 | 删除场次 |
| `POST /screening/countToday` | 是 | 统计今日场次数 |

### 票务模块 `/ticket`

| 接口 | 认证 | 说明 |
|---|---|---|
| `POST /ticket/list` | 是 | 分页查询票务信息 |
| `POST /ticket/listBySid` | 是 | 按场次查询座位票信息 |
| `POST /ticket/build` | 是 | 创建票（选座） |
| `POST /ticket/useTicket` | 是 | 使用/核销票 |
| `POST /ticket/autoUse` | 是 | 自动核销已过期的票 |

### 订单模块 `/order`

| 接口 | 认证 | 说明 |
|---|---|---|
| `POST /order/build` | 是 | 创建订单 |
| `POST /order/pay` | 是 | 确认支付订单 |
| `POST /order/refund` | 是 | 退款/取消订单 |
| `POST /order/list` | 是 | 分页查询用户订单 |
| `POST /order/countToday` | 是 | 统计今日订单数 |
| `POST /order/countTodaySales` | 是 | 统计今日销售额 |
| `POST /order/refundRate` | 是 | 查询退款率 |

### 文件上传 `/upload`

| 接口 | 认证 | 说明 |
|---|---|---|
| `POST /upload` | 是 | 上传电影封面图片（最大 10MB） |

## 统一响应格式

所有接口返回统一的 `RV<T>` 结构：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

## 数据库

数据库名：`cinema`，连接地址默认为 `localhost:3306`。主要数据表如下：

| 表名 | 说明 |
|---|---|
| `user` | 用户信息 |
| `admin` | 管理员信息 |
| `movie` | 电影信息 |
| `cinema` | 影院信息 |
| `screenroom` | 放映厅信息 |
| `screening` | 放映场次信息 |
| `ticket` | 票务信息 |
| `order` | 订单信息 |

## License

本项目仅供学习交流使用。
