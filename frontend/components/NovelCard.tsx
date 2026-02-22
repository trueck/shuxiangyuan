"use client"

import { Novel, Collection } from "@/lib/api"
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { BookOpen, Star, ExternalLink } from "lucide-react"
import { useState } from "react"

interface NovelCardProps {
  collection: Collection
  onEdit?: (collection: Collection) => void
  onDelete?: (id: number) => void
}

export function NovelCard({ collection, onEdit, onDelete }: NovelCardProps) {
  const { novel, rating, reading_status, current_chapter, category } = collection

  const handleReadClick = () => {
    window.open(novel.source_url, "_blank")
  }

  const getReadingStatusText = (status: string) => {
    switch (status) {
      case "reading":
        return "阅读中"
      case "completed":
        return "已完结"
      case "dropped":
        return "已弃坑"
      default:
        return status
    }
  }

  const getReadingStatusColor = (status: string) => {
    switch (status) {
      case "reading":
        return "bg-blue-100 text-blue-700 dark:bg-blue-900 dark:text-blue-300"
      case "completed":
        return "bg-green-100 text-green-700 dark:bg-green-900 dark:text-green-300"
      case "dropped":
        return "bg-gray-100 text-gray-700 dark:bg-gray-800 dark:text-gray-300"
      default:
        return "bg-gray-100 text-gray-700"
    }
  }

  return (
    <Card className="h-full flex flex-col">
      <CardHeader>
        <div className="flex items-start justify-between gap-2">
          <CardTitle className="line-clamp-2">{novel.title}</CardTitle>
          {rating && (
            <div className="flex items-center gap-1 text-sm font-medium text-amber-600 shrink-0">
              <Star className="h-4 w-4 fill-current" />
              {rating}
            </div>
          )}
        </div>
        {novel.author && (
          <p className="text-sm text-muted-foreground">{novel.author}</p>
        )}
      </CardHeader>

      <CardContent className="flex-1 space-y-3">
        {novel.description && (
          <p className="text-sm text-muted-foreground line-clamp-3">
            {novel.description}
          </p>
        )}

        <div className="flex flex-wrap gap-2">
          <span className={`text-xs px-2 py-1 rounded-full ${getReadingStatusColor(reading_status)}`}>
            {getReadingStatusText(reading_status)}
          </span>
          {category && (
            <span className="text-xs px-2 py-1 rounded-full bg-secondary text-secondary-foreground">
              {category.name}
            </span>
          )}
          {novel.source_site && (
            <span className="text-xs px-2 py-1 rounded-full bg-muted text-muted-foreground">
              {novel.source_site}
            </span>
          )}
        </div>

        {current_chapter > 0 && (
          <p className="text-xs text-muted-foreground">
            读至第 {current_chapter} 章
          </p>
        )}
      </CardContent>

      <CardFooter className="flex gap-2">
        <Button
          variant="default"
          size="sm"
          className="flex-1"
          onClick={handleReadClick}
        >
          <ExternalLink className="h-4 w-4 mr-1" />
          阅读
        </Button>
        {onEdit && (
          <Button
            variant="outline"
            size="sm"
            onClick={() => onEdit(collection)}
          >
            编辑
          </Button>
        )}
        {onDelete && (
          <Button
            variant="destructive"
            size="sm"
            onClick={() => onDelete(collection.id)}
          >
            删除
          </Button>
        )}
      </CardFooter>
    </Card>
  )
}
