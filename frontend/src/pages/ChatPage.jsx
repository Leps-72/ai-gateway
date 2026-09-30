import { Bot, CornerDownLeft, Sparkles } from 'lucide-react'
import EmptyState from '../components/common/EmptyState'
import PageHeader from '../components/common/PageHeader'

function ChatPage() {
  return (
    <div className="page-stack playground-page">
      <PageHeader
        eyebrow="Playground"
        title="AI Chat"
        description="Send a prompt through the gateway and inspect the provider response."
      />

      <section className="card playground-card">
        <div className="playground-toolbar">
          <div className="provider-label"><Sparkles size={17} /><span>Provider</span><strong>Gemini</strong></div>
          <span className="badge badge-neutral">Model configured by gateway</span>
        </div>
        <div className="conversation-panel">
          <EmptyState
            icon={Bot}
            title="Start a conversation"
            description="Chat history will appear here after API integration is enabled."
          />
        </div>
        <div className="composer">
          <label className="sr-only" htmlFor="chat-message">Message</label>
          <textarea id="chat-message" rows="4" placeholder="Ask the gateway anything…" disabled />
          <div className="composer-footer">
            <span>API connection is not enabled in this foundation step.</span>
            <button className="button button-primary" type="button" disabled>
              Send <CornerDownLeft size={17} />
            </button>
          </div>
        </div>
      </section>
    </div>
  )
}

export default ChatPage
