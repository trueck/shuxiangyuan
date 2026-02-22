// API 客户端配置
const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api';

export const api = {
  // 基础请求方法
  async request<T>(endpoint: string, options?: RequestInit): Promise<T> {
    const url = `${API_BASE_URL}${endpoint}`;

    const defaultOptions: RequestInit = {
      headers: {
        'Content-Type': 'application/json',
      },
    };

    // 添加认证 token (仅在客户端)
    if (typeof window !== 'undefined') {
      const token = localStorage.getItem('token');
      if (token) {
        (defaultOptions.headers as Record<string, string>)['Authorization'] = `Bearer ${token}`;
      }
    }

    const response = await fetch(url, { ...defaultOptions, ...options });

    if (!response.ok) {
      const error = await response.json().catch(() => ({ message: '请求失败' }));
      throw new Error(error.message || '请求失败');
    }

    const result = await response.json();

    // 后端返回格式: { success: true, message: "...", data: ... }
    // 自动提取 data 字段
    if (result && typeof result === 'object' && 'data' in result) {
      return result.data as T;
    }

    return result as T;
  },

  // GET 请求
  get<T>(endpoint: string): Promise<T> {
    return this.request<T>(endpoint, { method: 'GET' });
  },

  // POST 请求
  post<T>(endpoint: string, data?: unknown): Promise<T> {
    return this.request<T>(endpoint, {
      method: 'POST',
      body: JSON.stringify(data),
    });
  },

  // PUT 请求
  put<T>(endpoint: string, data?: unknown): Promise<T> {
    return this.request<T>(endpoint, {
      method: 'PUT',
      body: JSON.stringify(data),
    });
  },

  // DELETE 请求
  delete<T>(endpoint: string): Promise<T> {
    return this.request<T>(endpoint, { method: 'DELETE' });
  },
};

// 类型定义
export interface User {
  id: number;
  username: string;
  email: string;
  avatar_url?: string;
}

export interface AuthResponse {
  token: string;
  user: User;
}

export interface Novel {
  id: number;
  title: string;
  author?: string;
  description?: string;
  cover_url?: string;
  source_url: string;
  source_site?: string;
  total_chapters: number;
  status?: string;
}

export interface Category {
  id: number;
  user_id: number;
  name: string;
  color?: string;
  icon?: string;
}

export interface Collection {
  id: number;
  user_id: number;
  novel: Novel;
  category_id?: number;
  category?: Category;
  rating?: number;
  reading_status: 'reading' | 'completed' | 'dropped';
  current_chapter: number;
  notes?: string;
}

// API 方法
export const authApi = {
  register: (data: { username: string; email: string; password: string }) =>
    api.post<{ token: string; user: User }>('/auth/register', data),

  login: (data: { email: string; password: string }) =>
    api.post<{ token: string; user: User }>('/auth/login', data),

  me: () => api.get<User>('/auth/me'),
};

export const novelApi = {
  list: (params?: { page?: number; size?: number }) =>
    api.get<{ content: Novel[]; totalElements: number }>(`/novels?${new URLSearchParams(params as any).toString()}`),

  get: (id: number) => api.get<Novel>(`/novels/${id}`),

  create: (data: Partial<Novel>) =>
    api.post<Novel>('/novels', data),
};

export const collectionApi = {
  list: () => api.get<Collection[]>('/collections'),

  create: (data: { novel_id: number; category_id?: number }) =>
    api.post<Collection>('/collections', data),

  update: (id: number, data: Partial<Collection>) =>
    api.put<Collection>(`/collections/${id}`, data),

  delete: (id: number) => api.delete(`/collections/${id}`),
};

export const categoryApi = {
  list: () => api.get<Category[]>('/categories'),

  create: (data: { name: string; color?: string; icon?: string }) =>
    api.post<Category>('/categories', data),

  update: (id: number, data: Partial<Category>) =>
    api.put<Category>(`/categories/${id}`, data),

  delete: (id: number) => api.delete(`/categories/${id}`),
};

// 排行榜相关类型定义
export interface NovelInfo {
  rank: number;
  title: string;
  author: string;
  coverUrl?: string;
  sourceUrl: string;
  description?: string;
  status?: string;
  totalChapters?: number;
}

export interface RankingData {
  siteName: string;
  rankingType: string;
  title: string;
  novels: NovelInfo[];
  updatedAt: string;
}

export interface RankingSummary {
  siteName: string;
  rankingType: string;
  title: string;
  novelCount: number;
  updatedAt: string;
}

// 排行榜 API 方法
export const rankingApi = {
  getAll: () => api.get<RankingSummary[]>('/rankings'),

  getBySite: (siteName: string) => api.get<RankingSummary[]>(`/rankings/${siteName}`),

  get: (siteName: string, rankingType: string) =>
    api.get<RankingData>(`/rankings/${siteName}/${rankingType}`),

  fetch: (siteName: string, rankingType: string) =>
    api.post<void>(`/rankings/fetch/${siteName}/${rankingType}`),

  fetchAll: () => api.post<void>('/rankings/fetch-all'),
};
