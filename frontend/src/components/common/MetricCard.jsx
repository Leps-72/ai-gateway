function MetricCard({ icon: Icon, label, value, hint, loading = false }) {
  return (
    <article className="card metric-card" aria-busy={loading}>
      <div className="metric-heading">
        <span>{label}</span>
        {Icon && <Icon size={18} aria-hidden="true" />}
      </div>
      <div className="metric-value">
        {loading ? <span className="metric-skeleton" aria-label={`Loading ${label}`} /> : (value ?? '—')}
      </div>
      <p>{hint}</p>
    </article>
  )
}

export default MetricCard
