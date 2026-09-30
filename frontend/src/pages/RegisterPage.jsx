import { Boxes, CircleCheck } from 'lucide-react'
import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useNavigate } from 'react-router-dom'
import PasswordField from '../components/common/PasswordField'
import { useAuth } from '../context/AuthContext'

function RegisterPage() {
  const [form, setForm] = useState({ username: '', password: '', confirmPassword: '' })
  const [errors, setErrors] = useState({})
  const [formError, setFormError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const { register } = useAuth()
  const navigate = useNavigate()
  const update = (field) => (event) => setForm((current) => ({ ...current, [field]: event.target.value }))

  const handleSubmit = async (event) => {
    event.preventDefault()
    const nextErrors = {}
    if (!form.username.trim()) nextErrors.username = 'Username is required.'
    if (!form.password) nextErrors.password = 'Password is required.'
    else if (form.password.length < 6) nextErrors.password = 'Password must be at least 6 characters.'
    if (!form.confirmPassword) nextErrors.confirmPassword = 'Please confirm your password.'
    else if (form.password !== form.confirmPassword) nextErrors.confirmPassword = 'Passwords do not match.'
    setErrors(nextErrors)
    setFormError('')
    if (Object.keys(nextErrors).length > 0) return

    setIsSubmitting(true)
    try {
      await register(form.username.trim(), form.password)
      navigate('/login', {
        replace: true,
        state: { successMessage: 'Account created successfully. Please sign in.' },
      })
    } catch (error) {
      setFormError(error.response?.data?.message || 'Unable to create your account. Please try again.')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <main className="auth-page">
      <section className="auth-brand-panel">
        <div className="auth-brand"><div className="brand-mark"><Boxes size={22} /></div><span>AI Gateway</span></div>
        <div className="auth-message">
          <span className="auth-kicker"><CircleCheck size={16} /> Built for reliable AI access</span>
          <h1>Create your<br />gateway workspace.</h1>
          <p>Start with a secure account foundation for protected AI requests, history and usage insights.</p>
        </div>
        <p className="auth-caption">Management Console · v1.0</p>
      </section>
      <section className="auth-form-panel">
        <div className="auth-card">
          <div className="auth-heading"><p className="eyebrow">Get started</p><h2>Create your account</h2><p>Create credentials for the protected AI Gateway console.</p></div>
          {formError && <div className="form-notice error" role="alert">{formError}</div>}
          <form onSubmit={handleSubmit} noValidate>
            <div className="form-field">
              <label htmlFor="username">Username</label>
              <input id="username" name="username" value={form.username} onChange={update('username')} autoComplete="username" required disabled={isSubmitting} aria-invalid={Boolean(errors.username)} aria-describedby={errors.username ? 'username-error' : undefined} placeholder="Choose a username" />
              {errors.username && <span className="field-error" id="username-error">{errors.username}</span>}
            </div>
            <PasswordField id="password" label="Password" value={form.password} onChange={update('password')} autoComplete="new-password" error={errors.password} disabled={isSubmitting} />
            <PasswordField id="confirmPassword" label="Confirm password" value={form.confirmPassword} onChange={update('confirmPassword')} autoComplete="new-password" error={errors.confirmPassword} disabled={isSubmitting} />
            <button className="button button-primary button-block" type="submit" disabled={isSubmitting}>{isSubmitting ? 'Creating account…' : 'Create account'}</button>
          </form>
          <p className="auth-switch">Already have an account? <Link to="/login">Sign in</Link></p>
        </div>
      </section>
    </main>
  )
}

export default RegisterPage
