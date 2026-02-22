# 书香源 - 网络小说收藏管理系统

一个功能完整的网络小说收藏管理系统，支持用户注册登录、小说收藏、评分分类、排行榜展示等功能。

## 项目概述

书香源是一个为小说爱好者设计的收藏管理平台，帮助用户整理、评分、分类自己喜欢的网络小说，并提供各大小说网站的排行榜信息。

### 技术栈

**后端：**
- Java 17
- Spring Boot 3.2.0
- Spring Security 6.2.0
- Spring Data JPA + Hibernate
- PostgreSQL 数据库
- JWT 认证
- Jsoup 爬虫框架

**前端：**
- Next.js 14 (App Router)
- TypeScript
- Tailwind CSS
- shadcn/ui 组件库
- React Hooks

## 已实现功能

### 1. 用户认证系统 ✅

- 用户注册
- 用户登录
- JWT Token 认证
- 密码加密存储 (BCrypt)
- 登录状态持久化

**API 端点：**
```
POST /api/auth/register - 用户注册
POST /api/auth/login - 用户登录
GET  /api/auth/me - 获取当前用户信息
```

### 2. 小说收藏管理 ✅

- 添加小说到收藏夹
- 查看收藏列表
- 更新收藏信息（评分、阅读状态、当前章节）
- 删除收藏
- 阅读状态管理（连载中、已完结、已弃坑）

**API 端点：**
```
GET    /api/collections - 获取收藏列表
POST   /api/collections - 添加收藏
PUT    /api/collections/{id} - 更新收藏
DELETE /api/collections/{id} - 删除收藏
```

**收藏属性：**
- 评分：0-10分
- 阅读状态：reading（连载中）、completed（已完结）、dropped（已弃坑）
- 当前章节数
- 个人笔记

### 3. 分类管理 ✅

- 创建自定义分类
- 为收藏指定分类
- 分类颜色标记
- 分类图标设置

**API 端点：**
```
GET    /api/categories - 获取分类列表
POST   /api/categories - 创建分类
PUT    /api/categories/{id} - 更新分类
DELETE /api/categories/{id} - 删除分类
```

### 4. 排行榜系统 ✅

- 支持多个小说网站的排行榜
- 月票榜、点击榜、推荐榜、新书榜
- 定时自动更新
- 手动触发更新
- Cloudflare 保护检测和模拟数据降级

**支持网站：**
- 起点中文网 (qidian)
- 纵横中文网 (zongheng)
- 晋江文学城 (jjwxc)
- 17K小说网 (17k)
- 番茄小说 (fanqie)

**API 端点：**
```
GET /api/rankings - 获取所有排行榜概览（公开）
GET /api/rankings/{siteName} - 获取指定网站排行榜（公开）
GET /api/rankings/{siteName}/{rankingType} - 获取特定排行榜数据（公开）
POST /api/rankings/fetch/{siteName}/{rankingType} - 手动触发抓取（需认证）
POST /api/rankings/fetch-all - 抓取所有排行榜（需认证）
```

**定时任务：**
- 每天凌晨2点：抓取所有网站所有排行榜
- 每小时：更新热门榜单（点击榜）
- 每30分钟：更新起点月票榜

## 预留未实现功能

### 1. 小说详情页面 📋

- 小说完整信息展示
- 章节列表
- 章节阅读器
- 阅读进度同步

**数据结构已预留：**
```java
// Novel.java
- total_chapters: 总章节数
- source_url: 来源链接
- source_site: 来源网站
```

### 2. 评论与评分系统 📋

- 小说评论功能
- 收藏分享功能
- 评分统计和排名

### 3. 搜索功能 📋

- 全站小说搜索
- 按作者、分类、状态筛选
- 高级搜索（评分范围、章节数等）

### 4. 社交功能 📋

- 用户关注
- 收藏分享
- 阅读推荐

### 5. 数据导入导出 📋

- 批量导入收藏
- 导出收藏数据（JSON/CSV）
- 阅读统计报告

### 6. 移动端适配 📋

- 响应式设计优化
- PWA 支持
- 移动端专属功能

### 7. 通知系统 📋

- 小说更新提醒
- 关注用户动态
- 系统通知

### 8. 真实爬虫实现 📋

当前使用模拟数据，真实爬虫实现需要：

- **浏览器自动化**：使用 Selenium/Playwright 绕过 Cloudflare
- **专业爬虫服务**：ScraperAPI、ZenRows 等
- **官方 API 合作**：与小说网站建立合作获取数据

**已预留爬虫架构：**
- `Crawler` 接口
- `AbstractCrawler` 抽象基类
- `CrawlerFactory` 工厂类
- 各网站爬虫实现类

## 安装部署

### 环境要求

- Java 17+
- Node.js 18+
- PostgreSQL 14+
- Maven 3.8+

### 后端配置

1. 克隆项目
```bash
git clone <repository-url>
cd shuxiangyuan/backend
```

2. 配置数据库
```bash
# 创建数据库
createdb shuxiangyuan

# 配置 src/main/resources/application.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/shuxiangyuan
    username: your_username
    password: your_password
  jpa:
    hibernate:
      ddl-auto: update
```

3. 启动后端
```bash
mvn spring-boot:run
```

后端将运行在 `http://localhost:8080`

### 前端配置

1. 进入前端目录
```bash
cd ../frontend
```

2. 安装依赖
```bash
npm install
```

3. 启动开发服务器
```bash
npm run dev
```

前端将运行在 `http://localhost:3000`

## 使用指南

### 1. 注册账号

访问 `http://localhost:3000/register`，填写用户名、邮箱和密码完成注册。

### 2. 登录系统

访问 `http://localhost:3000/login`，使用邮箱和密码登录。

### 3. 添加收藏

1. 登录后进入"收藏夹"
2. 点击"添加收藏"
3. 填写小说信息（标题、作者、来源链接等）
4. 选择分类（可选）
5. 保存

### 4. 评分和管理

- 在收藏列表中点击小说卡片
- 设置评分（0-10分）
- 更新阅读状态
- 添加个人笔记

### 5. 查看排行榜

首页自动显示各网站的月票榜排行榜，包括：
- 小说排名
- 小说标题
- 作者
- 状态信息

## 数据库结构

### 用户表 (users)
```sql
id              BIGSERIAL PRIMARY KEY
username        VARCHAR(50) UNIQUE NOT NULL
email           VARCHAR(100) UNIQUE NOT NULL
password_hash   VARCHAR(255) NOT NULL
avatar_url      VARCHAR(255)
created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
```

### 小说表 (novels)
```sql
id              BIGSERIAL PRIMARY KEY
title           VARCHAR(255) NOT NULL
author          VARCHAR(100)
description     TEXT
cover_url       VARCHAR(500)
source_url      VARCHAR(500) NOT NULL
source_site     VARCHAR(50)
total_chapters  INTEGER DEFAULT 0
status          VARCHAR(20)
created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
```

### 分类表 (categories)
```sql
id          BIGSERIAL PRIMARY KEY
user_id     BIGINT REFERENCES users(id)
name        VARCHAR(50) NOT NULL
color       VARCHAR(20)
icon        VARCHAR(50)
created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
```

### 收藏表 (collections)
```sql
id              BIGSERIAL PRIMARY KEY
user_id         BIGINT REFERENCES users(id)
novel_id        BIGINT REFERENCES novels(id)
category_id     BIGINT REFERENCES categories(id)
rating          DECIMAL(2,1) CHECK (rating >= 0 AND rating <= 10)
reading_status  VARCHAR(20) CHECK (reading_status IN ('reading', 'completed', 'dropped'))
current_chapter INTEGER DEFAULT 0
notes           TEXT
created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
```

### 排行榜表 (rankings)
```sql
id            BIGSERIAL PRIMARY KEY
site_name     VARCHAR(50) NOT NULL
ranking_type  VARCHAR(50) NOT NULL
title         VARCHAR(255)
novels        JSONB NOT NULL
updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
UNIQUE (site_name, ranking_type)
```

## API 文档

### 认证相关

#### 注册用户
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "testuser",
  "email": "test@example.com",
  "password": "password123"
}

Response:
{
  "success": true,
  "message": "注册成功",
  "data": {
    "token": "eyJhbGciOiJIUzM4NCJ9...",
    "user": {
      "id": 1,
      "username": "testuser",
      "email": "test@example.com"
    }
  }
}
```

#### 用户登录
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "password123"
}

Response:
{
  "success": true,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzM4NCJ9...",
    "user": {
      "id": 1,
      "username": "testuser",
      "email": "test@example.com"
    }
  }
}
```

### 收藏管理

#### 获取收藏列表
```http
GET /api/collections
Authorization: Bearer {token}

Response:
{
  "success": true,
  "data": [
    {
      "id": 1,
      "novel": {
        "id": 1,
        "title": "完美世界",
        "author": "辰东",
        "source_url": "https://www.qidian.com/info/1010734496"
      },
      "rating": 9.5,
      "reading_status": "reading",
      "current_chapter": 150
    }
  ]
}
```

#### 添加收藏
```http
POST /api/collections
Authorization: Bearer {token}
Content-Type: application/json

{
  "novel_id": 1,
  "category_id": 1
}

Response:
{
  "success": true,
  "message": "添加成功",
  "data": {
    "id": 1,
    "novel_id": 1,
    "category_id": 1
  }
}
```

### 排行榜

#### 获取所有排行榜概览（公开API）
```http
GET /api/rankings

Response:
{
  "success": true,
  "data": [
    {
      "siteName": "qidian",
      "rankingType": "monthly",
      "title": "起点中文网 - 月票榜",
      "novelCount": 5,
      "updatedAt": "2024-02-22T17:15:49"
    }
  ]
}
```

#### 获取特定排行榜详情（公开API）
```http
GET /api/rankings/qidian/monthly

Response:
{
  "success": true,
  "data": {
    "siteName": "qidian",
    "rankingType": "monthly",
    "title": "起点中文网 - 月票榜",
    "novels": [
      {
        "rank": 1,
        "title": "完美世界",
        "author": "辰东",
        "coverUrl": "",
        "sourceUrl": "https://www.qidian.com/info/1010734496",
        "description": "一粒尘可填海...",
        "status": "完结",
        "totalChapters": 2000
      }
    ],
    "updatedAt": "2024-02-22T17:15:49"
  }
}
```

#### 手动触发抓取（需认证）
```http
POST /api/rankings/fetch/qidian/monthly
Authorization: Bearer {token}

Response:
{
  "success": true,
  "message": "操作成功"
}
```

## 项目结构

```
shuxiangyuan/
├── backend/                    # 后端项目
│   ├── src/main/java/com/shuxiangyuan/
│   │   ├── auth/              # 认证相关
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   └── SecurityConfig.java
│   │   ├── config/            # 配置类
│   │   ├── controller/        # REST 控制器
│   │   │   ├── AuthController.java
│   │   │   ├── CollectionController.java
│   │   │   ├── NovelController.java
│   │   │   ├── CategoryController.java
│   │   │   └── RankingController.java
│   │   ├── crawler/           # 爬虫模块
│   │   │   ├── Crawler.java              # 爬虫接口
│   │   │   ├── AbstractCrawler.java      # 抽象基类
│   │   │   ├── CrawlerFactory.java       # 工厂类
│   │   │   ├── model/
│   │   │   │   └── NovelInfo.java        # 小说信息DTO
│   │   │   └── impl/
│   │   │       ├── QidianCrawler.java
│   │   │       ├── ZonghengCrawler.java
│   │   │       ├── JjwxcCrawler.java
│   │   │       ├── SeventeenK_Crawler.java
│   │   │       └── FanqieCrawler.java
│   │   ├── dto/               # 数据传输对象
│   │   │   └── ApiResponse.java
│   │   ├── entity/            # 实体类
│   │   │   ├── User.java
│   │   │   ├── Novel.java
│   │   │   ├── Category.java
│   │   │   ├── Collection.java
│   │   │   └── Ranking.java
│   │   ├── repository/        # 数据访问层
│   │   │   ├── UserRepository.java
│   │   │   ├── NovelRepository.java
│   │   │   ├── CategoryRepository.java
│   │   │   ├── CollectionRepository.java
│   │   │   └── RankingRepository.java
│   │   ├── scheduler/         # 定时任务
│   │   │   └── RankingScheduler.java
│   │   ├── security/          # 安全配置
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   └── SecurityConfig.java
│   │   ├── service/           # 业务逻辑层
│   │   │   ├── AuthService.java
│   │   │   ├── NovelService.java
│   │   │   ├── CollectionService.java
│   │   │   ├── CategoryService.java
│   │   │   └── RankingService.java
│   │   └── ShuxiangyuanApplication.java
│   ├── src/main/resources/
│   │   └── application.yml    # 应用配置
│   └── pom.xml                # Maven 配置
├── frontend/                   # 前端项目
│   ├── app/                    # Next.js App Router
│   │   ├── auth/              # 认证页面
│   │   │   ├── login/
│   │   │   └── register/
│   │   ├── collections/       # 收藏管理
│   │   ├── categories/        # 分类管理
│   │   ├── layout.tsx         # 根布局
│   │   └── page.tsx           # 首页
│   ├── components/            # React 组件
│   │   └── ui/                # shadcn/ui 组件
│   ├── lib/                   # 工具库
│   │   ├── api.ts             # API 客户端
│   │   └── auth-context.tsx   # 认证上下文
│   ├── package.json
│   └── tailwind.config.ts
└── README.md                   # 项目文档
```

## 开发注意事项

### 爬虫实现说明

当前爬虫使用模拟数据，原因是：

1. **Cloudflare 保护**：目标网站使用 Cloudflare 机器人检测
2. **JavaScript 渲染**：页面内容通过 JavaScript 动态加载
3. **反爬虫机制**：IP 限制、请求频率限制等

**如需实现真实爬虫，建议：**
- 使用 Selenium/Playwright 进行浏览器自动化
- 使用专业爬虫服务（ScraperAPI、ZenRows）
- 申请官方 API 接口

当前实现已预留完整的爬虫架构，可以方便地接入真实数据源。

### SSR 问题处理

前端代码已处理 Next.js SSR 问题：
- `localStorage` 访问通过 `typeof window !== 'undefined'` 保护
- API 调用仅在客户端执行

### 数据库 JSONB 类型

Ranking 实体使用 `@JdbcTypeCode(SqlTypes.JSON)` 注解处理 JSONB 类型：
```java
@JdbcTypeCode(SqlTypes.JSON)
@Column(columnDefinition = "jsonb")
private String novels;
```

## 许可证

MIT License

## 贡献指南

欢迎提交 Issue 和 Pull Request！

## 联系方式

如有问题或建议，请通过 GitHub Issues 联系。
