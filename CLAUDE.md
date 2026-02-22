# 书香源 (Shuxiangyuan) - Development Guide

## Project Overview

书香源 is a full-stack novel collection management system for web fiction enthusiasts. Users can register/login, collect novels, rate and categorize them, and view rankings from various novel websites.

**Technology Stack:**
- **Backend:** Java 17, Spring Boot 3.2.0, Spring Security 6.2.0, Spring Data JPA, PostgreSQL, JWT (JJWT 0.12.3), Jsoup (web scraping)
- **Frontend:** Next.js 14 (App Router), TypeScript, React 19, Tailwind CSS 4.2, shadcn/ui components
- **Database:** PostgreSQL 14+ with JSONB support

## Project Structure

```
shuxiangyuan/
├── backend/                    # Spring Boot backend
│   ├── src/main/java/com/shuxiangyuan/
│   │   ├── auth/              # JWT authentication filter
│   │   ├── config/            # Jackson configuration
│   │   ├── controller/        # REST controllers
│   │   ├── crawler/           # Web scraping framework
│   │   ├── dto/               # Data transfer objects
│   │   ├── entity/            # JPA entities
│   │   ├── repository/        # JPA repositories
│   │   ├── scheduler/         # Scheduled tasks
│   │   ├── security/          # Security config & JWT
│   │   ├── service/           # Business logic
│   │   └── ShuxiangyuanApplication.java
│   ├── src/main/resources/
│   │   └── application.yml    # Backend configuration
│   ├── pom.xml                # Maven dependencies
│   └── package.json           # Node.js for build tools
├── frontend/                   # Next.js frontend
│   ├── app/                    # App Router pages
│   ├── components/            # React components
│   ├── lib/                   # Utilities & API client
│   ├── package.json
│   └── tailwind.config.ts
├── docker-compose.yml         # PostgreSQL setup
└── README.md                   # User documentation
```

## Development Commands

### Prerequisites

- Java 17+
- Node.js 18+
- Maven 3.8+
- PostgreSQL 14+ (or use Docker)

### Database Setup

**Option 1: Using Docker (Recommended)**
```bash
# From project root
docker-compose up -d

# Database will be available at:
# Host: localhost:5432
# Database: shuxiangyuan
# Username: shuxiangyuan
# Password: shuxiangyuan123
```

**Option 2: Local PostgreSQL**
```bash
createdb shuxiangyuan
# Update src/main/resources/application.yml with your credentials
```

### Backend Development

```bash
cd backend

# Run backend (default port: 8080)
mvn spring-boot:run

# Build for production
mvn clean package

# Run tests
mvn test

# Skip tests during build
mvn clean package -DskipTests
```

Backend will run on `http://localhost:8080`

### Frontend Development

```bash
cd frontend

# Install dependencies (first time only)
npm install

# Run development server (default port: 3000)
npm run dev

# Build for production
npm run build

# Run production build
npm run start

# Lint code
npm run lint
```

Frontend will run on `http://localhost:3000`

### Environment Variables

Copy `.env.example` to `.env` and configure:

**Frontend (.env):**
```
NEXT_PUBLIC_API_URL=http://localhost:8080/api
```

**Backend (application.yml or environment):**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/shuxiangyuan
    username: shuxiangyuan
    password: shuxiangyuan123

jwt:
  secret: shuxiangyuan-secret-key-change-in-production-min-256-bits
  expiration: 86400000  # 24 hours
```

## High-Level Architecture

### Backend Architecture (Spring Boot)

**Layer Structure:**
1. **Controller Layer** - REST API endpoints
   - `AuthController` - User authentication (register, login, me)
   - `CollectionController` - Novel collection CRUD
   - `CategoryController` - Category management
   - `NovelController` - Novel catalog
   - `RankingController` - Novel ranking data (public API)

2. **Service Layer** - Business logic
   - `AuthService` - Authentication and user management
   - `CollectionService` - Collection operations
   - `CategoryService` - Category operations
   - `NovelService` - Novel catalog operations
   - `RankingService` - Ranking data management and crawler orchestration

3. **Repository Layer** - Data access (Spring Data JPA)
   - `UserRepository`, `NovelRepository`, `CollectionRepository`, `CategoryRepository`, `RankingRepository`, `ReadingHistoryRepository`

4. **Security Layer** - JWT-based authentication
   - `SecurityConfig` - Spring Security configuration with JWT filter
   - `JwtAuthenticationFilter` - Validates JWT tokens on protected routes
   - `JwtTokenProvider` - Generates and validates JWT tokens

**API Response Format:**
All endpoints return: `{ success: boolean, message: string, data: any }`

**Public vs Protected Routes:**
- Public: `/api/auth/**`, `/api/rankings/**`
- Protected: All other routes require JWT token in `Authorization: Bearer {token}` header

### Frontend Architecture (Next.js 14 App Router)

**Key Patterns:**

1. **Client-Side Rendering ("use client")**
   - Most components use `"use client"` directive
   - Authentication state managed in `AuthContext` (not React Server Components compatible)

2. **SSR-Safe localStorage Access**
   - Always check `typeof window !== 'undefined'` before accessing localStorage
   - Example in `lib/api.ts` and `lib/auth-context.tsx`:
     ```typescript
     if (typeof window !== 'undefined') {
       const token = localStorage.getItem('token');
     }
     ```

3. **Authentication Flow**
   - `AuthProvider` wraps the app in `app/layout.tsx`
   - Login stores token and user in localStorage
   - API client automatically includes token in requests
   - `useAuth()` hook provides authentication state

4. **API Client** (`lib/api.ts`)
   - Base URL from `NEXT_PUBLIC_API_URL` environment variable
   - Auto-extracts `data` field from backend responses
   - Type-safe API methods for each endpoint

5. **Routing**
   - File-based routing in `app/` directory
   - Pages: `page.tsx`, `login/page.tsx`, `register/page.tsx`, `collections/`, `categories/`

### Database Schema (PostgreSQL)

**Key Tables:**
- `users` - User accounts with BCrypt password hashes
- `novels` - Novel catalog (title, author, description, source_url, etc.)
- `categories` - User-defined categories for organizing collections
- `collections` - User's novel collections with ratings, reading status, notes
- `rankings` - Novel ranking data from various websites (uses JSONB)
- `reading_history` - Reading progress tracking (reserved for future use)

**JSONB Usage:**
The `rankings` table stores novel list data as JSONB:
```java
@JdbcTypeCode(SqlTypes.JSON)
@Column(columnDefinition = "jsonb")
private String novels;  // Stores JSON array of NovelInfo objects
```

This allows efficient storage and querying of complex nested data structures.

## Important Implementation Details

### 1. Web Crawler Pattern

The crawler system uses a factory pattern with abstract base classes:

**Interface:** `Crawler.java`
- `crawlRanking(String rankingType)` - Main crawling method
- `getSiteName()` - Site identifier
- `getSupportedRankingTypes()` - Available ranking types

**Abstract Base Class:** `AbstractCrawler.java`
- Template method pattern for crawling workflow
- Built-in retry mechanism with exponential backoff
- Random User-Agent rotation
- Fallback to mock data when scraping fails
- Helper methods for HTML parsing (Jsoup)

**Factory:** `CrawlerFactory.java`
- Spring auto-registers all `Crawler` implementations
- Returns crawler by site name
- Lists supported sites

**Supported Sites:**
- `qidian` - 起点中文网
- `zongheng` - 纵横中文网
- `jjwxc` - 晋江文学城
- `17k` - 17K小说网
- `fanqie` - 番茄小说

**Ranking Types:**
- `monthly` - Monthly voting ranking
- `click` - Click/popularity ranking
- `recommend` - Recommendation ranking
- `new` - New books ranking

**Current Implementation:**
All crawlers return mock data because:
- Target sites use Cloudflare bot protection
- Pages require JavaScript rendering
- Real implementation would require Selenium/Playwright or paid scraping APIs

The architecture is ready for real implementation - just override `parseRankingDocument()` in each crawler.

### 2. Scheduled Tasks

**RankingScheduler** automatically updates ranking data:

- **Daily at 2 AM** - Fetch all rankings from all sites
- **Every hour** - Update click rankings
- **Every 30 minutes** - Update Qidian monthly ranking

Manual trigger available via API: `POST /api/rankings/fetch-all` (requires authentication)

### 3. Security Configuration

**Spring Security Setup:**
- CSRF disabled (stateless API)
- CORS enabled for `localhost:3000` and `localhost:3001`
- JWT stateless session management
- Public endpoints: `/api/auth/**`, `/api/rankings/**`
- Protected endpoints: All others require valid JWT

**Password Storage:**
- BCrypt encoder with default strength (10 rounds)
- Never store plain text passwords

### 4. Entity Relationships

**Collection → Novel (Many-to-One)**
- Users collect novels with personal metadata
- Same novel can be in multiple users' collections

**Collection → Category (Many-to-One)**
- User can categorize their collections
- Categories are user-specific

**Novel Metadata:**
- `source_url` - Link to original novel page
- `source_site` - Website identifier
- `total_chapters` - For future reading progress feature
- `status` - Novel status (ongoing, completed, etc.)

### 5. Frontend shadcn/ui Components

UI components are in `components/ui/` (shadcn/ui pattern):
- Not pre-built with CLI - manually created
- Tailwind CSS with class variance authority for variants
- Examples: Button, Card, Dialog, Input, Label, etc.

Custom components in `components/`:
- `NovelCard` - Novel collection display with rating
- `RatingStars` - Interactive star rating component
- `EditCollectionDialog` - Modal for editing collection details
- `Header` - Navigation header with user menu

### 6. TypeScript Type Safety

**Frontend Types** (defined in `lib/api.ts`):
- `User` - User profile
- `Novel` - Novel information
- `Category` - Category definition
- `Collection` - User's novel collection
- `NovelInfo` - Ranking novel entry
- `RankingData` - Full ranking with novels
- `RankingSummary` - Ranking metadata

**Backend DTOs:**
- `ApiResponse<T>` - Generic API response wrapper
- `AuthResponse` - Login/register response with token
- `LoginRequest`, `RegisterRequest` - Authentication requests

## API Endpoints Reference

### Authentication (Public)
```
POST /api/auth/register  - Register new user
POST /api/auth/login     - Login user
GET  /api/auth/me        - Get current user (protected)
```

### Collections (Protected)
```
GET    /api/collections       - Get user's collections
POST   /api/collections       - Add to collection
PUT    /api/collections/{id}  - Update collection
DELETE /api/collections/{id}  - Remove from collection
```

### Categories (Protected)
```
GET    /api/categories       - Get user's categories
POST   /api/categories       - Create category
PUT    /api/categories/{id}  - Update category
DELETE /api/categories/{id}  - Delete category
```

### Novels (Protected)
```
GET  /api/novies          - List novels (paginated)
GET  /api/novels/{id}     - Get novel details
POST /api/novels          - Create novel entry
```

### Rankings (Public)
```
GET  /api/rankings                                  - Get all rankings overview
GET  /api/rankings/{siteName}                       - Get site's rankings
GET  /api/rankings/{siteName}/{rankingType}         - Get specific ranking
POST /api/rankings/fetch/{siteName}/{rankingType}   - Fetch ranking (protected)
POST /api/rankings/fetch-all                        - Fetch all (protected)
```

## Testing

### Backend Testing
```bash
cd backend
mvn test
```

### Frontend Testing
Currently no automated tests. Manual testing via:
1. Run `npm run dev`
2. Open `http://localhost:3000`
3. Register/login flow
4. Create collections
5. View rankings

## Production Deployment Considerations

1. **Change JWT secret** - Use a strong, unique secret key (min 256 bits)
2. **Database credentials** - Use strong passwords
3. **CORS configuration** - Update allowed origins for production domain
4. **Build optimization** - Run `npm run build` for production-optimized frontend
5. **Environment variables** - Never commit `.env` files
6. **Real crawler implementation** - Replace mock data with actual scraping or official APIs

## Troubleshooting

**Backend won't start:**
- Check PostgreSQL is running: `docker ps` or `pg_isready`
- Verify database credentials in `application.yml`
- Check port 8080 is not in use

**Frontend can't connect to backend:**
- Verify `NEXT_PUBLIC_API_URL` in `.env`
- Check backend is running on port 8080
- Check CORS configuration in `SecurityConfig.java`

**localStorage errors in browser:**
- Ensure `typeof window !== 'undefined'` check is present
- This is a common SSR issue in Next.js

**Rankings not updating:**
- Check logs for crawler errors
- Current implementation uses mock data (Cloudflare protection)
- Real scraping requires browser automation or paid APIs

## Key Files Reference

**Backend:**
- `src/main/java/com/shuxiangyuan/ShuxiangyuanApplication.java` - Main application entry point
- `src/main/java/com/shuxiangyuan/security/SecurityConfig.java` - Security & JWT configuration
- `src/main/java/com/shuxiangyuan/crawler/` - Web scraping framework
- `src/main/resources/application.yml` - Backend configuration
- `pom.xml` - Maven dependencies

**Frontend:**
- `app/layout.tsx` - Root layout with AuthProvider
- `lib/api.ts` - API client with TypeScript types
- `lib/auth-context.tsx` - Authentication state management
- `package.json` - npm dependencies
- `next.config.ts` - Next.js configuration

**Infrastructure:**
- `docker-compose.yml` - PostgreSQL container setup
- `.env.example` - Environment variable template
