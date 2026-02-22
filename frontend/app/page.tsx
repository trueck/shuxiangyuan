"use client"

import { useAuth } from "@/lib/auth-context"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { BookOpen, Star, TrendingUp, Clock, Plus } from "lucide-react"
import Link from "next/link"
import { useEffect, useState } from "react"
import { useRouter } from "next/navigation"
import { api, rankingApi, collectionApi, type Collection, type RankingData, type NovelInfo } from "@/lib/api"

// 网站显示名称和颜色配置
const SITE_CONFIG: Record<string, { name: string; color: string }> = {
  qidian: { name: "起点中文网", color: "bg-red-500" },
  zongheng: { name: "纵横中文网", color: "bg-blue-500" },
  jjwxc: { name: "晋江文学城", color: "bg-pink-500" },
  "17k": { name: "17K小说网", color: "bg-orange-500" },
  fanqie: { name: "番茄小说", color: "bg-purple-500" },
}

export default function Home() {
  const { user, isAuthenticated } = useAuth()
  const router = useRouter()
  const [recentCollections, setRecentCollections] = useState<Collection[]>([])
  const [rankings, setRankings] = useState<RankingData[]>([])
  const [loadingRankings, setLoadingRankings] = useState(true)
  const [loading, setLoading] = useState(false)
  const [jsLoaded, setJsLoaded] = useState(false)
  const [collectingNovels, setCollectingNovels] = useState<Set<string>>(new Set())

  // Check if JS is loaded
  useEffect(() => {
    setJsLoaded(true)
    console.log("=== React component mounted ===")
  }, [])

  useEffect(() => {
    console.log("=== useEffect triggered, isAuthenticated:", isAuthenticated)
    if (isAuthenticated) {
      loadRecentCollections()
    }
    // 总是加载排行榜数据
    loadRankings()
  }, [isAuthenticated])

  const loadRecentCollections = async () => {
    setLoading(true)
    try {
      const data = await api.get<Collection[]>("/collections")
      const recent = data
        .sort((a: any, b: any) => new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime())
        .slice(0, 5)
      setRecentCollections(recent)
    } catch (error) {
      console.error("Failed to load recent collections:", error)
    } finally {
      setLoading(false)
    }
  }

  const loadRankings = async () => {
    console.log("=== loadRankings called ===")
    setLoadingRankings(true)
    try {
      // 获取所有排行榜概览
      console.log("Fetching ranking summaries...")
      const summaries = await rankingApi.getAll()
      console.log("Summaries received:", summaries)

      // 如果没有数据，显示模拟数据
      if (!summaries || summaries.length === 0) {
        console.log("No rankings found, showing mock data")
        setRankings([])
        setLoadingRankings(false)
        return
      }

      // 为每个网站获取月票榜数据
      const rankingPromises = summaries
        .filter(s => s.rankingType === 'monthly') // 只显示月票榜
        .map(async (summary) => {
          try {
            console.log(`Fetching ${summary.siteName} ranking...`)
            return await rankingApi.get(summary.siteName, summary.rankingType)
          } catch (error) {
            console.error(`Failed to load ${summary.siteName} ranking:`, error)
            return null
          }
        })

      const results = await Promise.all(rankingPromises)
      const validRankings = results.filter((r): r is RankingData => r !== null)

      console.log("Final rankings:", validRankings)
      setRankings(validRankings)
    } catch (error) {
      console.error("Failed to load rankings:", error)
      setRankings([])
    } finally {
      setLoadingRankings(false)
    }
  }

  // 从排行榜添加收藏
  const handleCollectFromRanking = async (novel: NovelInfo) => {
    const novelKey = `${novel.title}-${novel.author}`

    if (!isAuthenticated) {
      // 保存待收藏信息到sessionStorage
      sessionStorage.setItem('pendingCollect', JSON.stringify({
        title: novel.title,
        author: novel.author,
        description: novel.description,
        source_url: novel.sourceUrl,
        source_site: novel.sourceUrl?.includes('qidian') ? 'qidian' :
                  novel.sourceUrl?.includes('zongheng') ? 'zongheng' :
                  novel.sourceUrl?.includes('jjwxc') ? 'jjwxc' :
                  novel.sourceUrl?.includes('17k') ? '17k' : 'fanqie',
        total_chapters: novel.totalChapters || 0,
        status: novel.status || '未知'
      }))
      // 跳转到登录页
      router.push('/login')
      return
    }

    setCollectingNovels(prev => new Set([...prev, novelKey]))

    try {
      // 首先检查小说是否已存在于数据库中
      let novelId: number
      try {
        // 尝试通过source_url查找现有小说
        const existingNovels = await api.get<{ content: any[]; totalElements: number }>(
          `/novels?source_url=${encodeURIComponent(novel.sourceUrl)}`
        )

        if (existingNovels.content && existingNovels.content.length > 0) {
          novelId = existingNovels.content[0].id
        } else {
          // 小说不存在，创建新小说
          const newNovel = await api.post<{
            id: number
          }>('/novels', {
            title: novel.title,
            author: novel.author,
            description: novel.description,
            source_url: novel.sourceUrl,
            source_site: novel.sourceUrl?.includes('qidian') ? 'qidian' :
                      novel.sourceUrl?.includes('zongheng') ? 'zongheng' :
                      novel.sourceUrl?.includes('jjwxc') ? 'jjwxc' :
                      novel.sourceUrl?.includes('17k') ? '17k' : 'fanqie',
            total_chapters: novel.totalChapters || 0,
            status: novel.status || '未知'
          })
          novelId = newNovel.id
        }
      } catch (error) {
        console.error('Error checking/creating novel:', error)
        // 创建小说
        const newNovel = await api.post<{
          id: number
        }>('/novels', {
          title: novel.title,
          author: novel.author,
          description: novel.description,
          source_url: novel.sourceUrl,
          source_site: 'unknown',
          total_chapters: novel.totalChapters || 0,
          status: novel.status || '未知'
        })
        novelId = newNovel.id
      }

      // 添加到收藏
      await collectionApi.create({
        novel_id: novelId
      })

      alert(`已收藏《${novel.title}》`)
    } catch (error: any) {
      console.error('收藏失败:', error)
      if (error.message?.includes('已存在')) {
        alert(`《${novel.title}》已在收藏夹中`)
      } else {
        alert('收藏失败，请稍后重试')
      }
    } finally {
      setCollectingNovels(prev => {
        const newSet = new Set(prev)
        newSet.delete(novelKey)
        return newSet
      })
    }
  }

  return (
    <main className="min-h-screen">

      {/* Hero Section */}
      <section className="bg-gradient-to-b from-primary/10 to-background py-16 md:py-24">
        <div className="container max-w-6xl mx-auto px-4 text-center">
          <div className="flex justify-center mb-6">
            <div className="p-4 bg-primary/20 rounded-full">
              <BookOpen className="h-12 w-12 text-primary" />
            </div>
          </div>
          <h1 className="text-4xl md:text-6xl font-bold mb-6">
            书香源
          </h1>
          <p className="text-xl text-muted-foreground mb-8 max-w-2xl mx-auto">
            网络小说收藏管理系统 - 收藏、评分、分类，一站式管理你的阅读生活
          </p>
          {!isAuthenticated ? (
            <div className="flex gap-4 justify-center">
              <Link href="/register">
                <Button size="lg" className="text-lg px-8">
                  立即开始
                </Button>
              </Link>
              <Link href="/login">
                <Button size="lg" variant="outline" className="text-lg px-8">
                  登录账号
                </Button>
              </Link>
            </div>
          ) : (
            <div className="flex gap-4 justify-center">
              <Link href="/collections">
                <Button size="lg" className="text-lg px-8">
                  我的收藏
                </Button>
              </Link>
              <Link href="/categories">
                <Button size="lg" variant="outline" className="text-lg px-8">
                  分类管理
                </Button>
              </Link>
            </div>
          )}
        </div>
      </section>

      {/* Features */}
      <section className="py-16 border-b">
        <div className="container max-w-6xl mx-auto px-4">
          <h2 className="text-3xl font-bold text-center mb-12">核心功能</h2>
          <div className="grid md:grid-cols-3 gap-8">
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Star className="h-5 w-5 text-yellow-500" />
                  收藏管理
                </CardTitle>
              </CardHeader>
              <CardContent>
                <p className="text-muted-foreground">
                  添加你喜欢的小说到收藏夹，随时追踪阅读进度
                </p>
              </CardContent>
            </Card>
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <TrendingUp className="h-5 w-5 text-blue-500" />
                  评分系统
                </CardTitle>
              </CardHeader>
              <CardContent>
                <p className="text-muted-foreground">
                  0-10分精准评分，记录你的阅读感受和推荐指数
                </p>
              </CardContent>
            </Card>
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <BookOpen className="h-5 w-5 text-green-500" />
                  自定义分类
                </CardTitle>
              </CardHeader>
              <CardContent>
                <p className="text-muted-foreground">
                  创建个性化分类，用颜色和图标整理你的书架
                </p>
              </CardContent>
            </Card>
          </div>
        </div>
      </section>

      {/* User's Recent Reading - 仅登录用户可见 */}
      {isAuthenticated && recentCollections.length > 0 && (
        <section className="py-16 border-b">
          <div className="container max-w-6xl mx-auto px-4">
            <div className="flex items-center gap-2 mb-8">
              <Clock className="h-6 w-6 text-primary" />
              <h2 className="text-3xl font-bold">最近阅读</h2>
            </div>
            <div className="grid md:grid-cols-5 gap-6">
              {recentCollections.map((collection) => (
                <Link key={collection.id} href={`/collections`}>
                  <Card className="hover:shadow-lg transition-shadow cursor-pointer">
                    <CardContent className="p-4">
                      <div className="aspect-[3/4] bg-gradient-to-br from-primary/20 to-primary/5 rounded-lg mb-3 flex items-center justify-center">
                        <BookOpen className="h-12 w-12 text-primary/30" />
                      </div>
                      <h3 className="font-semibold text-sm mb-1 line-clamp-1">
                        {collection.novel.title}
                      </h3>
                      <p className="text-xs text-muted-foreground line-clamp-1">
                        {collection.novel.author}
                      </p>
                      {collection.rating && (
                        <div className="flex items-center gap-1 mt-2">
                          <Star className="h-3 w-3 fill-yellow-400 text-yellow-400" />
                          <span className="text-xs font-medium">{collection.rating}</span>
                        </div>
                      )}
                    </CardContent>
                  </Card>
                </Link>
              ))}
            </div>
          </div>
        </section>
      )}

      {/* Rankings */}
      <section className="py-16">
        <div className="container max-w-6xl mx-auto px-4">
          <div className="flex items-center gap-2 mb-8">
            <TrendingUp className="h-6 w-6 text-primary" />
            <h2 className="text-3xl font-bold">热门排行榜</h2>
          </div>

          {loadingRankings ? (
            <div className="text-center text-muted-foreground py-12">
              加载排行榜数据中...
            </div>
          ) : rankings.length === 0 ? (
            <div className="text-center py-12">
              <p className="text-muted-foreground mb-4">
                暂无排行榜数据，请稍后再试
              </p>
              <Button
                variant="outline"
                onClick={() => loadRankings()}
              >
                重新加载
              </Button>
            </div>
          ) : (
            <>
              <p className="text-muted-foreground mb-8">
                来源于各大小说网站的月票榜单（实时更新）
              </p>
              <div className="grid lg:grid-cols-3 gap-8">
                {rankings.map((ranking) => {
                  const config = SITE_CONFIG[ranking.siteName] || { name: ranking.siteName, color: 'bg-gray-500' }
                  return (
                    <Card key={`${ranking.siteName}-${ranking.rankingType}`}>
                      <CardHeader>
                        <CardTitle className="flex items-center gap-2">
                          <div className={`w-3 h-3 rounded-full ${config.color}`} />
                          {config.name}
                          {ranking.novels.length > 0 && (
                            <span className="text-xs text-muted-foreground ml-2">
                              {new Date(ranking.updatedAt).toLocaleDateString()}
                            </span>
                          )}
                        </CardTitle>
                      </CardHeader>
                      <CardContent>
                        {ranking.novels.length > 0 ? (
                          <ul className="space-y-3">
                            {ranking.novels.map((novel) => {
                              const novelKey = `${novel.title}-${novel.author}`
                              const isCollecting = collectingNovels.has(novelKey)
                              return (
                                <li key={novel.rank} className="flex items-center gap-3 group">
                                  <span className={`flex-shrink-0 w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold ${
                                    novel.rank <= 3 ? 'bg-primary text-primary-foreground' : 'bg-muted'
                                  }`}>
                                    {novel.rank}
                                  </span>
                                  <div className="flex-1 min-w-0">
                                    <p className="font-medium text-sm truncate">{novel.title}</p>
                                    <p className="text-xs text-muted-foreground">{novel.author}</p>
                                  </div>
                                  <span className="text-xs text-muted-foreground flex-shrink-0">
                                    {novel.status}
                                  </span>
                                  <Button
                                    size="sm"
                                    variant="ghost"
                                    className="flex-shrink-0 h-7 px-2 opacity-0 group-hover:opacity-100 transition-opacity ml-auto"
                                    onClick={() => handleCollectFromRanking(novel)}
                                    disabled={isCollecting}
                                  >
                                    {isCollecting ? (
                                      <span className="text-xs">收藏中...</span>
                                    ) : (
                                      <>
                                        <Plus className="h-3 w-3 mr-1" />
                                        <span className="text-xs">收藏</span>
                                      </>
                                    )}
                                  </Button>
                                </li>
                              )
                            })}
                          </ul>
                        ) : (
                          <p className="text-sm text-muted-foreground">暂无数据</p>
                        )}
                      </CardContent>
                    </Card>
                  )
                })}
              </div>
            </>
          )}
        </div>
      </section>

      {/* CTA Section */}
      {!isAuthenticated && (
        <section className="py-16 bg-muted/50">
          <div className="container max-w-4xl mx-auto px-4 text-center">
            <h2 className="text-3xl font-bold mb-4">开始管理你的阅读生活</h2>
            <p className="text-muted-foreground mb-8">
              注册账号，即可收藏、评分、分类你喜欢的所有小说
            </p>
            <Link href="/register">
              <Button size="lg" className="text-lg px-8">
                免费注册
              </Button>
            </Link>
          </div>
        </section>
      )}
    </main>
  )
}
