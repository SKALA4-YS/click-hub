import { describe, expect, it } from 'vitest'

import router, { routes } from '@/router'

describe('shared shell routes', () => {
  it('uses unique paths and names for every top-level route', () => {
    const paths = routes.map(({ path }) => path)
    const names = routes.map(({ name }) => name)

    expect(new Set(paths).size).toBe(paths.length)
    expect(new Set(names).size).toBe(names.length)
  })

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
