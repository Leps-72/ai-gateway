const numberFormatter = new Intl.NumberFormat('en-US', {
  maximumFractionDigits: 0,
})

const latencyFormatter = new Intl.NumberFormat('en-US', {
  maximumFractionDigits: 1,
})

const percentageFormatter = new Intl.NumberFormat('en-US', {
  minimumFractionDigits: 0,
  maximumFractionDigits: 1,
})

export function formatNumber(value) {
  return numberFormatter.format(Number(value ?? 0))
}

export function formatLatency(value) {
  return `${latencyFormatter.format(Number(value ?? 0))} ms`
}

export function formatPercentage(value) {
  return `${percentageFormatter.format(Number(value ?? 0) * 100)}%`
}

export function formatCurrency(value) {
  const numericValue = Number(value)
  if (numericValue === 0) return '$0.00'

  return `$${numericValue.toLocaleString('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 6,
  })}`
}
