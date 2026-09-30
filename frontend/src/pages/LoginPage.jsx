import { Boxes, ShieldCheck } from 'lucide-react'
import { useState } from 'react'
import { Link } from 'react-router-dom'
import PasswordField from '../components/common/PasswordField'

function LoginPage() {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [errors, setErrors] = useState({})

  const handleSubmit = (event) => {
    event.preventDefault()
    const nextErrors = {}
    if (!username.trim()) nextErrors.username = 'Username is required.'
    if (!password) nextErrors.password = 'Password is required.'
    else if (password.length < 6) nextErrors.password = 'Password must be at least 6 characters.'
    setErrors(nextErrors)
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
          <div className="auth-heading"><p className="eyebrow">Welcome back</p><h2>Sign in to your workspace</h2><p>Authentication will be connected in the next implementation step.</p></div>
          <form onSubmit={handleSubmit} noValidate>
            <div className="form-field">
              <label htmlFor="username">Username</label>
              <input id="username" name="username" value={username} onChange={(event) => setUsername(event.target.value)} autoComplete="username" required aria-invalid={Boolean(errors.username)} aria-describedby={errors.username ? 'username-error' : undefined} placeholder="Enter your username" />
              {errors.username && <span className="field-error" id="username-error">{errors.username}</span>}
            </div>
            <PasswordField id="password" label="Password" value={password} onChange={(event) => setPassword(event.target.value)} autoComplete="current-password" error={errors.password} />
            <button className="button button-primary button-block" type="submit">Sign in</button>
          </form>
          <p className="auth-switch">New to AI Gateway? <Link to="/register">Create an account</Link></p>
        </div>
      </section>
    </main>
  )
}

export default LoginPage
