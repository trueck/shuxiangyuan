"use client"

import Link from "next/link"
import { Button } from "@/components/ui/button"
import { BookOpen, LogOut, User } from "lucide-react"
import { useAuth } from "@/lib/auth-context"

export function Header() {
  const { user, logout } = useAuth()

  return (
    <header className="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div className="container flex h-16 items-center justify-between">
        <Link href="/" className="flex items-center gap-2">
          <BookOpen className="h-6 w-6" />
          <span className="text-xl font-bold">书香源</span>
        </Link>

        <nav className="flex items-center gap-4">
          <Link href="/collections">
            <Button variant="ghost">收藏夹</Button>
          </Link>
          <Link href="/categories">
            <Button variant="ghost">分类管理</Button>
          </Link>
          {user ? (
            <>
              <div className="flex items-center gap-2 text-sm text-muted-foreground">
                <User className="h-4 w-4" />
                <span>{user.username}</span>
              </div>
              <Button variant="outline" size="sm" onClick={logout}>
                <LogOut className="h-4 w-4 mr-2" />
                登出
              </Button>
            </>
          ) : (
            <Link href="/login">
              <Button>登录</Button>
            </Link>
          )}
        </nav>
      </div>
    </header>
  )
}
