"use client"

import { useState } from "react"
import Link from "next/link"
import { useRouter } from "next/navigation"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { api } from "@/lib/api"
import { useAuth } from "@/lib/auth-context"

export default function LoginPage() {
  const router = useRouter()
  const { login } = useAuth()
  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [error, setError] = useState("")
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError("")
    setLoading(true)

    try {
      const response = await api.post<{ token: string; user: { id: number; username: string; email: string } }>("/auth/login", { email, password })

      // 使用 AuthContext 的 login 方法
      login(response.token, response.user)

      // 检查是否有待收藏的小说
      const pendingCollect = sessionStorage.getItem('pendingCollect')
      if (pendingCollect) {
        try {
          const pendingNovel = JSON.parse(pendingCollect)

          // 首先检查或创建小说
          let novelId: number
          try {
            const existingNovels = await api.get<{ content: any[]; totalElements: number }>(
              `/novels?source_url=${encodeURIComponent(pendingNovel.source_url)}`
            )

            if (existingNovels.content && existingNovels.content.length > 0) {
              novelId = existingNovels.content[0].id
            } else {
              const newNovel = await api.post<{ id: number }>('/novels', pendingNovel)
              novelId = newNovel.id
            }
          } catch (error) {
            // 如果查找失败，直接创建小说
            const newNovel = await api.post<{ id: number }>('/novels', pendingNovel)
            novelId = newNovel.id
          }

          // 添加到收藏
          await api.post('/collections', { novel_id: novelId })

          // 清除待收藏信息
          sessionStorage.removeItem('pendingCollect')

          // 显示成功消息并跳转到收藏页
          alert(`已成功收藏《${pendingNovel.title}》`)
          router.push("/collections")
          return
        } catch (error) {
          console.error('自动收藏失败:', error)
          sessionStorage.removeItem('pendingCollect')
        }
      }

      // 正常跳转到首页
      router.push("/")
    } catch (err: any) {
      setError(err.message || "登录失败")
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="container flex h-[calc(100vh-4rem)] items-center justify-center">
      <Card className="w-full max-w-md">
        <CardHeader>
          <CardTitle>登录</CardTitle>
          <CardDescription>输入您的账号信息以登录书香源</CardDescription>
        </CardHeader>
        <form onSubmit={handleSubmit}>
          <CardContent className="space-y-4">
            {error && (
              <div className="rounded-md bg-destructive/15 p-3 text-sm text-destructive">
                {error}
              </div>
            )}
            <div className="space-y-2">
              <Label htmlFor="email">邮箱</Label>
              <Input
                id="email"
                type="email"
                placeholder="your@email.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="password">密码</Label>
              <Input
                id="password"
                type="password"
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>
          </CardContent>
          <CardFooter className="flex flex-col space-y-4">
            <Button type="submit" className="w-full" disabled={loading}>
              {loading ? "登录中..." : "登录"}
            </Button>
            <p className="text-sm text-muted-foreground">
              还没有账号？{" "}
              <Link href="/register" className="text-primary hover:underline">
                立即注册
              </Link>
            </p>
          </CardFooter>
        </form>
      </Card>
    </div>
  )
}
