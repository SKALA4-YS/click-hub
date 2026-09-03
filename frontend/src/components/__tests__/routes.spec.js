import { describe, expect, it } from 'vitest'

import router from '@/router'

describe('shared shell routes', () => {
  it.each([
    ['/signup', 'signup'],
    ['/rankings/developers', 'developer-rankings'],
    ['/developers/42', 'developer-detail'],
  ])('resolves %s to the %s lazy page route', (path, name) => {
    const resolved = router.resolve(path)

    expect(resolved.name).toBe(name)
    expect(resolved.matched[0].components.default).toEqual(expect.any(Function))
  })
})
