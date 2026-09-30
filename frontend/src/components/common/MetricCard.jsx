function MetricCard({ icon: Icon, label, hint }) {
  return (
    <article className="card metric-card">
      <div className="metric-heading">
        <span>{label}</span>
        {Icon && <Icon size={18} aria-hidden="true" />}
      </div>
      <div className="metric-value" aria-label={`${label} not loaded`}>—</div>
      <p>{hint}</p>
    </article>
  )
}

export default MetricCard
