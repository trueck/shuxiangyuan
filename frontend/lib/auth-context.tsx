"use client"

import { createContext, useContext, useState, useEffect, ReactNode } from "react"
import { useRouter } from "next/navigation"

interface UserInfo {
  id: number
  username: string
  email: string
}

interface AuthContextType {
  user: UserInfo | null
  login: (token: string, user: UserInfo) => void
  logout: () => void
  isAuthenticated: boolean
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserInfo | null>(null)
  const router = useRouter()

  // 初始化时从 localStorage 读取
  useEffect(() => {
    // 仅在客户端执行
    if (typeof window === 'undefined') return

    const userStr = localStorage.getItem("user")
    if (userStr) {
      try {
        setUser(JSON.parse(userStr))
      } catch {
        localStorage.removeItem("user")
        localStorage.removeItem("token")
      }
    }
  }, [])

  const login = (token: string, userData: UserInfo) => {
    if (typeof window !== 'undefined') {
      localStorage.setItem("token", token)
      localStorage.setItem("user", JSON.stringify(userData))
    }
    setUser(userData)
  }

  const logout = () => {
    if (typeof window !== 'undefined') {
      localStorage.removeItem("token")
      localStorage.removeItem("user")
    }
    setUser(null)
    router.push("/")
  }

  return (
    <AuthContext.Provider value={{ user, login, logout, isAuthenticated: !!user }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider")
  }
  return context
}
