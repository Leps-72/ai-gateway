import { Boxes, ShieldCheck } from 'lucide-react'
import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useLocation, useNavigate } from 'react-router-dom'
import PasswordField from '../components/common/PasswordField'
import { useAuth } from '../context/AuthContext'

function LoginPage() {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [errors, setErrors] = useState({})
  const [formError, setFormError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const handleSubmit = async (event) => {
    event.preventDefault()
    const nextErrors = {}
    if (!username.trim()) nextErrors.username = 'Username is required.'
    if (!password) nextErrors.password = 'Password is required.'
    else if (password.length < 6) nextErrors.password = 'Password must be at least 6 characters.'
    setErrors(nextErrors)
    setFormError('')
    if (Object.keys(nextErrors).length > 0) return

    setIsSubmitting(true)
    try {
      await login(username.trim(), password)
      navigate('/dashboard', { replace: true })
    } catch (error) {
      setFormError(error.response?.data?.message || 'Unable to sign in. Please try again.')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <main className="auth-page">
      <section className="auth-brand-panel">
        <div className="auth-brand">
          <div className="brand-mark"><Boxes size={22} /></div>
          <span>AI Gateway</span>
        </div>
        <div className="auth-message">
          <span className="auth-kicker"><ShieldCheck size={16} /> Secure developer platform</span>
          <h1>One gateway.<br />Every AI request.</h1>
          <p>Centralize authentication, provider access, reliability and usage visibility in one focused console.</p>
        </div>
        <p className="auth-caption">Management Console · v1.0</p>
      </section>
      <section className="auth-form-panel">
        <div className="auth-card">
          <div className="auth-heading"><p className="eyebrow">Welcome back</p><h2>Sign in to your workspace</h2><p>Use your AI Gateway account to access protected tools and metrics.</p></div>
          {location.state?.successMessage && <div className="form-notice success" role="status">{location.state.successMessage}</div>}
          {formError && <div className="form-notice error" role="alert">{formError}</div>}
          <form onSubmit={handleSubmit} noValidate>
            <div className="form-field">
              <label htmlFor="username">Username</label>
              <input id="username" name="username" value={username} onChange={(event) => setUsername(event.target.value)} autoComplete="username" required disabled={isSubmitting} aria-invalid={Boolean(errors.username)} aria-describedby={errors.username ? 'username-error' : undefined} placeholder="Enter your username" />
              {errors.username && <span className="field-error" id="username-error">{errors.username}</span>}
            </div>
            <PasswordField id="password" label="Password" value={password} onChange={(event) => setPassword(event.target.value)} autoComplete="current-password" error={errors.password} disabled={isSubmitting} />
            <button className="button button-primary button-block" type="submit" disabled={isSubmitting}>{isSubmitting ? 'Signing in…' : 'Sign in'}</button>
          </form>
          <p className="auth-switch">New to AI Gateway? <Link to="/register">Create an account</Link></p>
        </div>
      </section>
    </main>
  )
}

export default LoginPage
