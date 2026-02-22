"use client"

import { useEffect, useState } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { categoryApi, type Category } from "@/lib/api"
import { Plus, Pencil, Trash2 } from "lucide-react"
import Link from "next/link"
import { useRequireAuth } from "@/lib/use-require-auth"

export default function CategoriesPage() {
  useRequireAuth()
  const [categories, setCategories] = useState<Category[]>([])
  const [loading, setLoading] = useState(true)
  const [showAddForm, setShowAddForm] = useState(false)
  const [editingCategory, setEditingCategory] = useState<Category | null>(null)

  const [formData, setFormData] = useState({
    name: "",
    color: "#3b82f6",
    icon: "",
  })

  useEffect(() => {
    loadCategories()
  }, [])

  const loadCategories = async () => {
    try {
      const data = await categoryApi.list()
      setCategories(data)
    } catch (error) {
      console.error("Failed to load categories:", error)
    } finally {
      setLoading(false)
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    try {
      if (editingCategory) {
        await categoryApi.update(editingCategory.id, formData)
      } else {
        await categoryApi.create(formData)
      }
      loadCategories()
      resetForm()
    } catch (error: any) {
      alert(error.message || "操作失败")
    }
  }

  const handleEdit = (category: Category) => {
    setEditingCategory(category)
    setFormData({
      name: category.name,
      color: category.color || "#3b82f6",
      icon: category.icon || "",
    })
    setShowAddForm(true)
  }

  const handleDelete = async (id: number) => {
    if (!confirm("确定要删除这个分类吗？")) return

    try {
      await categoryApi.delete(id)
      setCategories(categories.filter((c) => c.id !== id))
    } catch (error: any) {
      alert(error.message || "删除失败")
    }
  }

  const resetForm = () => {
    setFormData({ name: "", color: "#3b82f6", icon: "" })
    setEditingCategory(null)
    setShowAddForm(false)
  }

  if (loading) {
    return (
      <div className="container py-8">
        <div className="text-center text-muted-foreground">加载中...</div>
      </div>
    )
  }

  return (
    <div className="container py-8 max-w-4xl">
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-3xl font-bold">分类管理</h1>
          <p className="text-muted-foreground mt-1">
            创建自定义分类来组织你的小说收藏
          </p>
        </div>
        <Button onClick={() => setShowAddForm(true)}>
          <Plus className="h-4 w-4 mr-2" />
          新建分类
        </Button>
      </div>

      {showAddForm && (
        <Card className="mb-6">
          <CardHeader>
            <CardTitle>{editingCategory ? "编辑分类" : "新建分类"}</CardTitle>
            <CardDescription>
              {editingCategory ? "修改分类信息" : "创建一个新的小说分类"}
            </CardDescription>
          </CardHeader>
          <form onSubmit={handleSubmit}>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="name">分类名称 *</Label>
                <Input
                  id="name"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  placeholder="例如：玄幻小说"
                  required
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="color">分类颜色</Label>
                <div className="flex gap-3 items-center">
                  <Input
                    id="color"
                    type="color"
                    value={formData.color}
                    onChange={(e) => setFormData({ ...formData, color: e.target.value })}
                    className="w-20 h-10 p-1"
                  />
                  <Input
                    type="text"
                    value={formData.color}
                    onChange={(e) => setFormData({ ...formData, color: e.target.value })}
                    placeholder="#3b82f6"
                    className="flex-1"
                  />
                </div>
              </div>

              <div className="space-y-2">
                <Label htmlFor="icon">图标 (可选)</Label>
                <Input
                  id="icon"
                  value={formData.icon}
                  onChange={(e) => setFormData({ ...formData, icon: e.target.value })}
                  placeholder="例如：📚"
                />
              </div>

              <div className="flex gap-3 pt-2">
                <Button type="submit" className="flex-1">
                  {editingCategory ? "保存" : "创建"}
                </Button>
                <Button type="button" variant="outline" onClick={resetForm} className="flex-1">
                  取消
                </Button>
              </div>
            </CardContent>
          </form>
        </Card>
      )}

      {categories.length === 0 ? (
        <Card>
          <CardContent className="text-center py-12">
            <p className="text-muted-foreground mb-4">还没有创建任何分类</p>
            <Button onClick={() => setShowAddForm(true)}>
              <Plus className="h-4 w-4 mr-2" />
              创建第一个分类
            </Button>
          </CardContent>
        </Card>
      ) : (
        <div className="grid gap-4 md:grid-cols-2">
          {categories.map((category) => (
            <Card key={category.id}>
              <CardHeader>
                <div className="flex items-start justify-between">
                  <div className="flex items-center gap-3">
                    {category.icon && (
                      <span className="text-2xl">{category.icon}</span>
                    )}
                    <div>
                      <CardTitle className="text-lg">{category.name}</CardTitle>
                      <div className="flex items-center gap-2 mt-1">
                        <div
                          className="w-4 h-4 rounded-full"
                          style={{ backgroundColor: category.color }}
                        />
                        <span className="text-xs text-muted-foreground">
                          {category.color}
                        </span>
                      </div>
                    </div>
                  </div>
                  <div className="flex gap-2">
                    <Button
                      variant="ghost"
                      size="icon"
                      onClick={() => handleEdit(category)}
                    >
                      <Pencil className="h-4 w-4" />
                    </Button>
                    <Button
                      variant="ghost"
                      size="icon"
                      onClick={() => handleDelete(category.id)}
                    >
                      <Trash2 className="h-4 w-4 text-destructive" />
                    </Button>
                  </div>
                </div>
              </CardHeader>
            </Card>
          ))}
        </div>
      )}

      <div className="mt-8">
        <Link href="/collections">
          <Button variant="outline">
            返回收藏夹
          </Button>
        </Link>
      </div>
    </div>
  )
}
