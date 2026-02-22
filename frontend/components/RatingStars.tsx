"use client"

import { Star } from "lucide-react"
import { useState } from "react"

interface RatingStarsProps {
  rating: number
  onRatingChange?: (rating: number) => void
  readonly?: boolean
  max?: number
}

export function RatingStars({ rating, onRatingChange, readonly = false, max = 10 }: RatingStarsProps) {
  const [hoverRating, setHoverRating] = useState(0)

  const handleClick = (value: number) => {
    if (!readonly && onRatingChange) {
      onRatingChange(value)
    }
  }

  const handleMouseEnter = (value: number) => {
    if (!readonly) {
      setHoverRating(value)
    }
  }

  const handleMouseLeave = () => {
    if (!readonly) {
      setHoverRating(0)
    }
  }

  const displayRating = hoverRating || rating

  return (
    <div className="flex items-center gap-1">
      <div className="flex">
        {Array.from({ length: max }).map((_, i) => {
          const value = i + 1
          const fillPercentage = Math.max(0, Math.min(100, (displayRating - i) * 100))

          return (
            <button
              key={i}
              type="button"
              disabled={readonly}
              onClick={() => handleClick(value)}
              onMouseEnter={() => handleMouseEnter(value)}
              onMouseLeave={handleMouseLeave}
              className={`relative ${readonly ? "cursor-default" : "cursor-pointer"} focus:outline-none`}
              aria-label={`Rate ${value} stars`}
            >
              <Star
                className="h-5 w-5"
                style={{
                  color: fillPercentage > 0 ? "#f59e0b" : "#d1d5db",
                }}
              />
            </button>
          )
        })}
      </div>
      <span className="ml-2 text-sm font-medium text-amber-600">
        {displayRating.toFixed(1)}
      </span>
    </div>
  )
}
