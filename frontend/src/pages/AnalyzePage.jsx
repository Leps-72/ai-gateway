import { AlignLeft, ScanSearch } from 'lucide-react'
import EmptyState from '../components/common/EmptyState'
import PageHeader from '../components/common/PageHeader'

const resultFields = ['Summary', 'Sentiment', 'Category', 'Priority']

function AnalyzePage() {
  return (
    <div className="page-stack">
      <PageHeader
        eyebrow="Playground"
        title="Analyze"
        description="Transform unstructured feedback into a consistent structured response."
      />

      <div className="analyze-grid">
        <section className="card form-card">
          <div className="section-heading compact">
            <div><p className="eyebrow">Input</p><h2>Text to analyze</h2></div>
            <AlignLeft size={20} aria-hidden="true" />
          </div>
          <label htmlFor="analysis-text">Customer feedback or application text</label>
          <textarea id="analysis-text" rows="11" placeholder="Paste text here…" disabled />
          <div className="form-footer">
            <span>Structured output: summary, sentiment, category and priority.</span>
            <button className="button button-primary" type="button" disabled><ScanSearch size={17} /> Analyze</button>
          </div>
        </section>

        <section className="card result-card">
          <div className="section-heading compact">
            <div><p className="eyebrow">Output</p><h2>Analysis result</h2></div>
          </div>
          <EmptyState
            icon={ScanSearch}
            title="No analysis yet"
            description="Results will appear after the analyze API is connected."
          />
          <div className="result-field-grid" aria-label="Expected result fields">
            {resultFields.map((field) => <div key={field}><span>{field}</span><strong>—</strong></div>)}
          </div>
        </section>
      </div>
    </div>
  )
}

export default AnalyzePage
