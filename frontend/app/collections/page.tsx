"use client"

import { useEffect, useState } from "react"
import { NovelCard } from "@/components/NovelCard"
import { EditCollectionDialog } from "@/components/EditCollectionDialog"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { collectionApi, categoryApi, type Collection, type Category } from "@/lib/api"
import { Plus, Search } from "lucide-react"
import Link from "next/link"
import { useRequireAuth } from "@/lib/use-require-auth"

export default function CollectionsPage() {
  useRequireAuth()
  const [collections, setCollections] = useState<Collection[]>([])
  const [categories, setCategories] = useState<Category[]>([])
  const [loading, setLoading] = useState(true)
  const [search, setSearch] = useState("")
  const [filterCategory, setFilterCategory] = useState<number | null>(null)
  const [editingCollection, setEditingCollection] = useState<Collection | null>(null)

  useEffect(() => {
    loadData()
  }, [])

  const loadData = async () => {
    try {
      const [collectionsData, categoriesData] = await Promise.all([
        collectionApi.list(),
        categoryApi.list(),
      ])
      setCollections(collectionsData)
      setCategories(categoriesData)
    } catch (error) {
      console.error("Failed to load data:", error)
    } finally {
      setLoading(false)
    }
  }

  const handleDelete = async (id: number) => {
    if (!confirm("确定要删除这个收藏吗？")) return

    try {
      await collectionApi.delete(id)
      setCollections(collections.filter((c) => c.id !== id))
    } catch (error: any) {
      alert(error.message || "删除失败")
    }
  }

  const handleEdit = (collection: Collection) => {
    setEditingCollection(collection)
  }

  const handleSave = () => {
    loadData()
  }

  const filteredCollections = collections.filter((collection) => {
    const matchesSearch =
      !search ||
      collection.novel.title.toLowerCase().includes(search.toLowerCase()) ||
      collection.novel.author?.toLowerCase().includes(search.toLowerCase())

    const matchesCategory =
      !filterCategory || collection.category_id === filterCategory

    return matchesSearch && matchesCategory
  })

  if (loading) {
    return (
      <div className="container py-8">
        <div className="text-center text-muted-foreground">加载中...</div>
      </div>
    )
  }

  return (
    <div className="container py-8">
      <div className="flex items-center justify-between mb-8">
        <h1 className="text-3xl font-bold">我的收藏</h1>
        <Link href="/collections/add">
          <Button>
            <Plus className="h-4 w-4 mr-2" />
            添加小说
          </Button>
        </Link>
      </div>

      <div className="flex flex-col sm:flex-row gap-4 mb-6">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <Input
            placeholder="搜索小说名称或作者..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="pl-10"
          />
        </div>

        <select
          value={filterCategory || ""}
          onChange={(e) => setFilterCategory(e.target.value ? Number(e.target.value) : null)}
          className="px-3 py-2 rounded-md border bg-background"
        >
          <option value="">全部分类</option>
          {categories.map((category) => (
            <option key={category.id} value={category.id}>
              {category.name}
            </option>
          ))}
        </select>
      </div>

      {filteredCollections.length === 0 ? (
        <div className="text-center py-12">
          <p className="text-muted-foreground mb-4">
            {search || filterCategory ? "没有找到匹配的小说" : "还没有收藏任何小说"}
          </p>
          {!search && !filterCategory && (
            <Link href="/collections/add">
              <Button>
                <Plus className="h-4 w-4 mr-2" />
                添加第一本小说
              </Button>
            </Link>
          )}
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          {filteredCollections.map((collection) => (
            <NovelCard
              key={collection.id}
              collection={collection}
              onEdit={handleEdit}
              onDelete={handleDelete}
            />
          ))}
        </div>
      )}

      {editingCollection && (
        <EditCollectionDialog
          collection={editingCollection}
          open={!!editingCollection}
          onClose={() => setEditingCollection(null)}
          onSave={handleSave}
        />
      )}
    </div>
  )
}
