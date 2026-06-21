export function SkeletonCard({ height = 'h-20' }) {
  return <div className={`${height} w-full bg-gray-100 rounded-lg animate-pulse`} />;
}

export function SkeletonText({ width = 'w-full' }) {
  return <div className={`h-4 ${width} bg-gray-200 rounded animate-pulse`} />;
}
