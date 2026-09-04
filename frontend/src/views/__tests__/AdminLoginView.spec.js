import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import AdminLoginView from '@/views/AdminLoginView.vue'

const auth = vi.hoisted(() => ({
  isAdmin: false,
  loading: false,
  loginAsAdmin: vi.fn(),
}))
const route = vi.hoisted(() => ({ query: {} }))
const replace = vi.hoisted(() => vi.fn())

vi.mock('@/stores/auth', () => ({ useAuthStore: () => auth }))
vi.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ replace }),
}))

describe('AdminLoginView', () => {
  beforeEach(() => {
    auth.isAdmin = false
    auth.loading = false
    auth.loginAsAdmin.mockReset().mockResolvedValue({ role: 'ADMIN' })
    route.query = {}
    replace.mockReset()
  })

  it('renders a dedicated ID and password login', () => {
    const wrapper = mount(AdminLoginView)

    expect(wrapper.get('h1').text()).toBe('관리자 로그인')
    expect(wrapper.get('input[name="username"]').attributes('autocomplete')).toBe('username')
    expect(wrapper.get('input[name="password"]').attributes('type')).toBe('password')
  })

  it('logs in and moves to the requested admin page', async () => {
    route.query = { redirect: '/admin/projects/project-1' }
    const wrapper = mount(AdminLoginView)

    await wrapper.get('input[name="username"]').setValue('admin')
    await wrapper.get('input[name="password"]').setValue('admin')
    await wrapper.get('form').trigger('submit')
    await flushPromises()

    expect(auth.loginAsAdmin).toHaveBeenCalledWith({ username: 'admin', password: 'admin' })
    expect(replace).toHaveBeenCalledWith('/admin/projects/project-1')
  })

  it('shows authentication failures without leaving the page', async () => {
    auth.loginAsAdmin.mockRejectedValue(new Error('관리자 ID 또는 비밀번호가 올바르지 않습니다.'))
    const wrapper = mount(AdminLoginView)

    await wrapper.get('input[name="username"]').setValue('admin')
    await wrapper.get('input[name="password"]').setValue('wrong')
    await wrapper.get('form').trigger('submit')
    await flushPromises()

    expect(wrapper.get('[role="alert"]').text()).toContain('올바르지 않습니다')
    expect(replace).not.toHaveBeenCalled()
  })

  it('moves an existing admin session directly to the approval page', () => {
    auth.isAdmin = true

    mount(AdminLoginView)

    expect(replace).toHaveBeenCalledWith('/admin/projects')
  })
})
