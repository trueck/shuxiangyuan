"use client"

import { useEffect, useState } from "react"

export default function TestPage() {
  const [count, setCount] = useState(0)

  useEffect(() => {
    console.log("Test page mounted!")
  }, [])

  return (
    <div className="p-8">
      <h1>Test Page</h1>
      <p>Count: {count}</p>
      <button onClick={() => setCount(count + 1)} className="px-4 py-2 bg-blue-500 text-white rounded">
        Increment
      </button>
      <p className="mt-4 text-green-500">If you can see the increment button working, client-side rendering is working!</p>
    </div>
  )
}
