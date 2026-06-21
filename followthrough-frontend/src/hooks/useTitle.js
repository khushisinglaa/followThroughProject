import { useEffect } from 'react';

export function useTitle(page) {
  useEffect(() => {
    document.title = page ? `${page} — FollowThrough` : 'FollowThrough';
  }, [page]);
}
