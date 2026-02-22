"use client"

import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { collectionApi, categoryApi, type Category } from "@/lib/api"
import { ArrowLeft } from "lucide-react"
import Link from "next/link"

export default function AddNovelPage() {
  const router = useRouter()
  const [categories, setCategories] = useState<Category[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState("")

  const [novelData, setNovelData] = useState({
    title: "",
    author: "",
    description: "",
    cover_url: "",
    source_url: "",
    source_site: "",
    total_chapters: 0,
    status: "",
  })

  const [categoryId, setCategoryId] = useState<number | null>(null)

  useEffect(() => {
    loadCategories()
  }, [])

  const loadCategories = async () => {
    try {
      const data = await categoryApi.list()
      setCategories(data)
    } catch (error) {
      console.error("Failed to load categories:", error)
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError("")

    if (!novelData.title || !novelData.source_url) {
      setError("请填写小说名称和来源链接")
      return
    }

    setLoading(true)

    try {
      // 首先创建小说
      const novel = await novelApi.create(novelData)

      // 然后添加到收藏
      await collectionApi.create({
        novel_id: novel.id,
        category_id: categoryId || undefined,
      })

      router.push("/collections")
    } catch (error: any) {
      setError(error.message || "添加失败")
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="container py-8 max-w-2xl">
      <Link href="/collections" className="inline-flex items-center text-sm text-muted-foreground hover:text-foreground mb-6">
        <ArrowLeft className="h-4 w-4 mr-1" />
        返回收藏夹
      </Link>

      <Card>
        <CardHeader>
          <CardTitle>添加小说</CardTitle>
          <CardDescription>手动添加一本新小说到收藏夹</CardDescription>
        </CardHeader>
        <form onSubmit={handleSubmit}>
          <CardContent className="space-y-4">
            {error && (
              <div className="rounded-md bg-destructive/15 p-3 text-sm text-destructive">
                {error}
              </div>
            )}

            <div className="space-y-2">
              <Label htmlFor="title">小说名称 *</Label>
              <Input
                id="title"
                value={novelData.title}
                onChange={(e) => setNovelData({ ...novelData, title: e.target.value })}
                placeholder="请输入小说名称"
                required
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="author">作者</Label>
              <Input
                id="author"
                value={novelData.author}
                onChange={(e) => setNovelData({ ...novelData, author: e.target.value })}
                placeholder="请输入作者名称"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="source_url">来源链接 *</Label>
              <Input
                id="source_url"
                type="url"
                value={novelData.source_url}
                onChange={(e) => setNovelData({ ...novelData, source_url: e.target.value })}
                placeholder="https://example.com/novel/123"
                required
              />
              <p className="text-xs text-muted-foreground">
                点击阅读按钮时会跳转到此链接
              </p>
            </div>

            <div className="space-y-2">
              <Label htmlFor="source_site">来源网站</Label>
              <Input
                id="source_site"
                value={novelData.source_site}
                onChange={(e) => setNovelData({ ...novelData, source_site: e.target.value })}
                placeholder="例如：起点中文网"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="description">简介</Label>
              <textarea
                id="description"
                value={novelData.description}
                onChange={(e) => setNovelData({ ...novelData, description: e.target.value })}
                placeholder="请输入小说简介"
                rows={3}
                className="flex min-h-[80px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="cover_url">封面图片链接</Label>
              <Input
                id="cover_url"
                type="url"
                value={novelData.cover_url}
                onChange={(e) => setNovelData({ ...novelData, cover_url: e.target.value })}
                placeholder="https://example.com/cover.jpg"
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="total_chapters">总章节数</Label>
                <Input
                  id="total_chapters"
                  type="number"
                  value={novelData.total_chapters}
                  onChange={(e) => setNovelData({ ...novelData, total_chapters: Number(e.target.value) })}
                  min="0"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="status">状态</Label>
                <select
                  id="status"
                  value={novelData.status}
                  onChange={(e) => setNovelData({ ...novelData, status: e.target.value })}
                  className="flex h-9 w-full rounded-md border border-input bg-background px-3 py-1 text-sm"
                >
                  <option value="">未知</option>
                  <option value="连载">连载中</option>
                  <option value="完结">已完结</option>
                </select>
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="category">分类</Label>
              <select
                id="category"
                value={categoryId || ""}
                onChange={(e) => setCategoryId(e.target.value ? Number(e.target.value) : null)}
                className="flex h-9 w-full rounded-md border border-input bg-background px-3 py-1 text-sm"
              >
                <option value="">未分类</option>
                {categories.map((category) => (
                  <option key={category.id} value={category.id}>
                    {category.name}
                  </option>
                ))}
              </select>
            </div>

            <div className="flex gap-4 pt-4">
              <Button type="submit" className="flex-1" disabled={loading}>
                {loading ? "添加中..." : "添加到收藏"}
              </Button>
              <Link href="/collections" className="flex-1">
                <Button type="button" variant="outline" className="w-full">
                  取消
                </Button>
              </Link>
            </div>
          </CardContent>
        </form>
      </Card>
    </div>
  )
}

// 添加 novelApi 到导入
import { novelApi } from "@/lib/api"
