"use client"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { collectionApi, categoryApi, type Collection, type Category } from "@/lib/api"
import { RatingStars } from "@/components/RatingStars"

interface EditCollectionDialogProps {
  collection: Collection
  open: boolean
  onClose: () => void
  onSave: () => void
}

export function EditCollectionDialog({ collection, open, onClose, onSave }: EditCollectionDialogProps) {
  const [categories, setCategories] = useState<Category[]>([])
  const [loading, setLoading] = useState(false)

  const [formData, setFormData] = useState({
    rating: collection.rating || 0,
    category_id: collection.category_id || null,
    reading_status: collection.reading_status,
    current_chapter: collection.current_chapter,
    notes: collection.notes || "",
  })

  useEffect(() => {
    if (open) {
      loadCategories()
    }
  }, [open])

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
    setLoading(true)

    try {
      // 转换数据类型以匹配 API
      const updateData = {
        rating: formData.rating,
        category_id: formData.category_id || undefined,
        reading_status: formData.reading_status,
        current_chapter: formData.current_chapter,
        notes: formData.notes,
      }
      await collectionApi.update(collection.id, updateData)
      onSave()
      onClose()
    } catch (error: any) {
      alert(error.message || "更新失败")
    } finally {
      setLoading(false)
    }
  }

  if (!open) return null

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div className="bg-background rounded-lg shadow-lg max-w-md w-full max-h-[90vh] overflow-y-auto">
        <div className="p-6">
          <h2 className="text-xl font-bold mb-4">编辑收藏</h2>
          <p className="text-sm text-muted-foreground mb-4">{collection.novel.title}</p>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
              <Label>评分</Label>
              <RatingStars
                rating={formData.rating}
                onRatingChange={(rating) => setFormData({ ...formData, rating })}
                max={10}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="category">分类</Label>
              <select
                id="category"
                value={formData.category_id || ""}
                onChange={(e) => setFormData({ ...formData, category_id: e.target.value ? Number(e.target.value) : null })}
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

            <div className="space-y-2">
              <Label htmlFor="status">阅读状态</Label>
              <select
                id="status"
                value={formData.reading_status}
                onChange={(e) => setFormData({ ...formData, reading_status: e.target.value as "reading" | "completed" | "dropped" })}
                className="flex h-9 w-full rounded-md border border-input bg-background px-3 py-1 text-sm"
              >
                <option value="reading">阅读中</option>
                <option value="completed">已完结</option>
                <option value="dropped">已弃坑</option>
              </select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="current_chapter">当前章节</Label>
              <Input
                id="current_chapter"
                type="number"
                value={formData.current_chapter}
                onChange={(e) => setFormData({ ...formData, current_chapter: Number(e.target.value) })}
                min="0"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="notes">备注</Label>
              <textarea
                id="notes"
                value={formData.notes}
                onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
                rows={3}
                placeholder="添加备注..."
                className="flex min-h-[80px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
              />
            </div>

            <div className="flex gap-3 pt-2">
              <Button type="submit" className="flex-1" disabled={loading}>
                {loading ? "保存中..." : "保存"}
              </Button>
              <Button type="button" variant="outline" onClick={onClose} className="flex-1">
                取消
              </Button>
            </div>
          </form>
        </div>
      </div>
    </div>
  )
}
