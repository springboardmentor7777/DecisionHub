import { useMemo, useState } from 'react'
import './DecisionWorkspace.css'

const initialDecisions = [
  {
    id: 'summit',
    title: 'Where should we host our annual community summit?',
    description:
      'Help us choose a format that makes it easy for more members to connect.',
    category: 'Community',
    deadline: '2 days left',
    author: 'Maya Chen',
    comments: 18,
    voters: 86,
    options: [
      { id: 'hybrid', label: 'Hybrid event', detail: 'In-person + livestream', votes: 42 },
      { id: 'in-person', label: 'In-person', detail: 'Meet together in one city', votes: 28 },
      { id: 'virtual', label: 'Virtual-first', detail: 'Join from anywhere', votes: 16 },
    ],
  },
  {
    id: 'launch',
    title: 'Which feature should we build next?',
    description:
      'Your feedback will help us prioritize the next product release.',
    category: 'Product',
    deadline: '5 days left',
    author: 'Jordan Lee',
    comments: 12,
    voters: 54,
    options: [
      { id: 'analytics', label: 'Decision analytics', detail: 'Compare results over time', votes: 29 },
      { id: 'teams', label: 'Team spaces', detail: 'Organize decisions by team', votes: 17 },
      { id: 'export', label: 'Report export', detail: 'Shareable PDF summaries', votes: 8 },
    ],
  },
  {
    id: 'volunteer',
    title: 'What should our next volunteer day support?',
    description:
      'Pick a cause for the neighborhood volunteer day this spring.',
    category: 'Community',
    deadline: '1 week left',
    author: 'Sam Rivera',
    comments: 9,
    voters: 38,
    options: [
      { id: 'garden', label: 'Community garden', detail: 'Plant and maintain shared spaces', votes: 21 },
      { id: 'food', label: 'Food bank', detail: 'Sort and pack local donations', votes: 11 },
      { id: 'cleanup', label: 'Park cleanup', detail: 'Make our parks greener', votes: 6 },
    ],
  },
]

function Icon({ name, size = 18 }) {
  const paths = {
    grid: <><rect x="3" y="3" width="7" height="7" rx="1.5" /><rect x="14" y="3" width="7" height="7" rx="1.5" /><rect x="3" y="14" width="7" height="7" rx="1.5" /><rect x="14" y="14" width="7" height="7" rx="1.5" /></>,
    compass: <><circle cx="12" cy="12" r="9" /><path d="m15.5 8.5-2.2 4.8-4.8 2.2 2.2-4.8 4.8-2.2Z" /></>,
    users: <><path d="M16 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" /><circle cx="10" cy="7" r="4" /><path d="M20 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75" /></>,
    chart: <><path d="M3 3v18h18" /><path d="m19 9-5 5-4-4-5 5" /></>,
    settings: <><circle cx="12" cy="12" r="3" /><path d="m19.4 15 .1.1 1.4 1.1-1.4 2.4-1.7-.7a8 8 0 0 1-1.5.9l-.3 1.8h-2.8l-.3-1.8a8 8 0 0 1-1.5-.9l-1.7.7-1.4-2.4 1.4-1.1a7 7 0 0 1 0-1.8l-1.4-1.1 1.4-2.4 1.7.7a8 8 0 0 1 1.5-.9l.3-1.8H16l.3 1.8a8 8 0 0 1 1.5.9l1.7-.7 1.4 2.4-1.4 1.1a7 7 0 0 1-.1 1.8Z" /></>,
    search: <><circle cx="11" cy="11" r="7" /><path d="m20 20-4-4" /></>,
    bell: <><path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9" /><path d="M10 21h4" /></>,
    plus: <><path d="M12 5v14M5 12h14" /></>,
    arrow: <><path d="M5 12h14M13 6l6 6-6 6" /></>,
    clock: <><circle cx="12" cy="12" r="9" /><path d="M12 7v5l3 2" /></>,
    message: <><path d="M21 11.5a8.4 8.4 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.4 8.4 0 0 1-3.8-.9L3 21l1.9-5.7a8.4 8.4 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.4 8.4 0 0 1 3.8-.9h.5a8.5 8.5 0 0 1 8 8v.5Z" /></>,
    close: <><path d="m18 6-12 12M6 6l12 12" /></>,
    spark: <><path d="m12 3 1.9 5.8L20 11l-6.1 2.2L12 19l-2-5.8L4 11l6-2.2L12 3Z" /><path d="m19 14 1.2 2.8L23 18l-2.8 1.2L19 22l-1.2-2.8L15 18l2.8-1.2L19 14Z" /></>,
  }

  return (
    <svg
      aria-hidden="true"
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.7"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      {paths[name]}
    </svg>
  )
}

function DecisionCard({ decision, myVote, onVote }) {
  const [selectedOption, setSelectedOption] = useState(myVote || '')
  const totalVotes = decision.options.reduce((total, option) => total + option.votes, 0)

  function submitVote() {
    if (!selectedOption) return
    onVote(decision.id, selectedOption)
  }

  return (
    <article className="dw-card">
      <div className="dw-card-heading">
        <span className={`dw-category dw-category-${decision.category.toLowerCase()}`}>
          {decision.category}
        </span>
        <span className="dw-deadline"><Icon name="clock" size={15} />{decision.deadline}</span>
      </div>
      <h3>{decision.title}</h3>
      <p className="dw-card-description">{decision.description}</p>
      <div className="dw-options" role="radiogroup" aria-label={`Options for ${decision.title}`}>
        {decision.options.map((option) => {
          const percentage = totalVotes ? Math.round((option.votes / totalVotes) * 100) : 0
          const isSelected = selectedOption === option.id

          return (
            <button
              aria-checked={isSelected}
              className={`dw-option${isSelected ? ' is-selected' : ''}`}
              key={option.id}
              onClick={() => setSelectedOption(option.id)}
              role="radio"
              type="button"
            >
              <span className="dw-option-copy">
                <span className="dw-radio" />
                <span className="dw-option-text">
                  <strong>{option.label}</strong>
                  <small>{option.detail}</small>
                </span>
                <span className="dw-option-percent">{percentage}%</span>
              </span>
              <span aria-hidden="true" className="dw-progress-track">
                <span className="dw-progress-fill" style={{ width: `${percentage}%` }} />
              </span>
            </button>
          )
        })}
      </div>
      <div className="dw-card-footer">
        <div className="dw-card-meta">
          <span className="dw-avatar-stack" aria-hidden="true">
            <span className="dw-mini-avatar dw-mini-one">M</span>
            <span className="dw-mini-avatar dw-mini-two">J</span>
            <span className="dw-mini-avatar dw-mini-three">S</span>
          </span>
          <span>{decision.voters} votes</span>
          <span className="dw-meta-divider">·</span>
          <span className="dw-comments"><Icon name="message" size={14} />{decision.comments}</span>
        </div>
        <button
          className={`dw-vote-button${myVote ? ' is-voted' : ''}`}
          disabled={!selectedOption}
          onClick={submitVote}
          type="button"
        >
          {myVote ? 'Update vote' : 'Cast your vote'}
          <Icon name="arrow" size={15} />
        </button>
      </div>
    </article>
  )
}

function CreateDecisionModal({ onClose, onCreate }) {
  function handleSubmit(event) {
    event.preventDefault()
    const formData = new FormData(event.currentTarget)
    onCreate({
      title: formData.get('title').trim(),
      description: formData.get('description').trim(),
      category: formData.get('category'),
      options: [
        formData.get('optionOne').trim(),
        formData.get('optionTwo').trim(),
      ],
    })
  }

  return (
    <div className="dw-modal-backdrop" onMouseDown={(event) => {
      if (event.target === event.currentTarget) onClose()
    }}>
      <section aria-labelledby="dw-modal-title" aria-modal="true" className="dw-modal" role="dialog">
        <div className="dw-modal-heading">
          <div>
            <span className="dw-eyebrow">YOUR COMMUNITY, YOUR CALL</span>
            <h2 id="dw-modal-title">Start a decision</h2>
          </div>
          <button aria-label="Close dialog" className="dw-icon-button" onClick={onClose} type="button">
            <Icon name="close" />
          </button>
        </div>
        <form className="dw-create-form" onSubmit={handleSubmit}>
          <label>
            Decision title
            <input autoFocus maxLength="120" name="title" placeholder="What should the community decide?" required />
          </label>
          <label>
            A little context
            <textarea maxLength="240" name="description" placeholder="Add a short description..." rows="3" />
          </label>
          <label>
            Category
            <select defaultValue="Community" name="category">
              <option>Community</option>
              <option>Product</option>
              <option>Events</option>
            </select>
          </label>
          <div className="dw-option-inputs">
            <label>
              Option one
              <input maxLength="60" name="optionOne" placeholder="First choice" required />
            </label>
            <label>
              Option two
              <input maxLength="60" name="optionTwo" placeholder="Second choice" required />
            </label>
          </div>
          <div className="dw-modal-actions">
            <button className="dw-cancel-button" onClick={onClose} type="button">Cancel</button>
            <button className="dw-primary-button" type="submit"><Icon name="plus" size={17} />Create decision</button>
          </div>
        </form>
      </section>
    </div>
  )
}

export default function DecisionWorkspace() {
  const [decisions, setDecisions] = useState(initialDecisions)
  const [myVotes, setMyVotes] = useState({})
  const [activeTab, setActiveTab] = useState('all')
  const [category, setCategory] = useState('All topics')
  const [search, setSearch] = useState('')
  const [isModalOpen, setIsModalOpen] = useState(false)

  const filteredDecisions = useMemo(() => decisions.filter((decision) => {
    const matchesTab =
      activeTab === 'all' ||
      (activeTab === 'needs-vote' && !myVotes[decision.id]) ||
      (activeTab === 'my-votes' && Boolean(myVotes[decision.id]))
    const matchesCategory = category === 'All topics' || decision.category === category
    const matchesSearch = `${decision.title} ${decision.description}`
      .toLowerCase()
      .includes(search.trim().toLowerCase())
    return matchesTab && matchesCategory && matchesSearch
  }), [activeTab, category, decisions, myVotes, search])

  function castVote(decisionId, optionId) {
    setDecisions((current) => current.map((decision) => {
      if (decision.id !== decisionId) return decision
      const previousVote = myVotes[decisionId]
      return {
        ...decision,
        voters: previousVote ? decision.voters : decision.voters + 1,
        options: decision.options.map((option) => ({
          ...option,
          votes:
            option.votes +
            (option.id === optionId ? 1 : 0) -
            (option.id === previousVote ? 1 : 0),
        })),
      }
    }))
    setMyVotes((current) => ({ ...current, [decisionId]: optionId }))
  }

  function createDecision({ title, description, category: newCategory, options }) {
    const id = `decision-${Date.now()}`
    setDecisions((current) => [{
      id,
      title,
      description: description || 'A new decision for the community to weigh in on.',
      category: newCategory,
      deadline: '7 days left',
      author: 'Alex Morgan',
      comments: 0,
      voters: 0,
      options: options.map((label, index) => ({
        id: `${id}-option-${index}`,
        label,
        detail: 'Share your preference',
        votes: 0,
      })),
    }, ...current])
    setActiveTab('all')
    setCategory('All topics')
    setSearch('')
    setIsModalOpen(false)
  }

  return (
    <div className="dw-app">
      <aside className="dw-sidebar">
        <a aria-label="DecisionHub home" className="dw-brand" href="#workspace">
          <span className="dw-brand-mark"><Icon name="spark" size={20} /></span>
          <span>decision<span className="dw-brand-light">hub</span></span>
        </a>
        <span className="dw-nav-label">WORKSPACE</span>
        <nav aria-label="Main navigation" className="dw-nav">
          <a className="dw-nav-link is-active" href="#workspace"><Icon name="grid" />Overview</a>
          <a className="dw-nav-link" href="#decisions"><Icon name="compass" />Explore decisions</a>
          <a className="dw-nav-link" href="#community"><Icon name="users" />Community</a>
          <a className="dw-nav-link" href="#insights"><Icon name="chart" />Insights</a>
        </nav>
        <div className="dw-sidebar-bottom">
          <div className="dw-sidebar-note">
            <span className="dw-note-icon"><Icon name="spark" size={17} /></span>
            <strong>Good decisions start here.</strong>
            <p>Bring people together and find a way forward.</p>
            <button onClick={() => setIsModalOpen(true)} type="button">Start a decision <Icon name="arrow" size={14} /></button>
          </div>
          <a className="dw-nav-link dw-settings-link" href="#settings"><Icon name="settings" />Settings</a>
          <div className="dw-profile">
            <span className="dw-profile-avatar">A</span>
            <span className="dw-profile-copy"><strong>Alex Morgan</strong><small>Community member</small></span>
            <span className="dw-profile-more">···</span>
          </div>
        </div>
      </aside>

      <main className="dw-main" id="workspace">
        <header className="dw-topbar">
          <div className="dw-breadcrumb"><span>Workspace</span><span>/</span><strong>Overview</strong></div>
          <div className="dw-topbar-actions">
            <label className="dw-search">
              <Icon name="search" size={17} />
              <input
                aria-label="Search decisions"
                onChange={(event) => setSearch(event.target.value)}
                placeholder="Search decisions..."
                value={search}
              />
              <kbd>/</kbd>
            </label>
            <button aria-label="Notifications" className="dw-icon-button dw-notification" type="button">
              <Icon name="bell" />
              <span />
            </button>
            <span aria-label="Alex Morgan" className="dw-profile-avatar dw-top-avatar">A</span>
          </div>
        </header>

        <div className="dw-content">
          <section className="dw-welcome">
            <div>
              <span className="dw-eyebrow"><Icon name="spark" size={14} />TUESDAY, NOVEMBER 10</span>
              <h1>Good morning, Alex <span>✳</span></h1>
              <p>Better decisions happen when everyone has a voice.</p>
            </div>
            <button className="dw-primary-button" onClick={() => setIsModalOpen(true)} type="button">
              <Icon name="plus" size={18} />New decision
            </button>
          </section>

          <section aria-label="Your community at a glance" className="dw-stat-grid">
            <article className="dw-stat-card"><span className="dw-stat-icon dw-stat-purple"><Icon name="compass" /></span><span className="dw-stat-label">Active decisions</span><strong>{decisions.length.toString().padStart(2, '0')}</strong><small><span className="dw-stat-dot" />Open for input</small></article>
            <article className="dw-stat-card"><span className="dw-stat-icon dw-stat-peach"><Icon name="users" /></span><span className="dw-stat-label">Community voices</span><strong>{decisions.reduce((sum, item) => sum + item.voters, 0)}</strong><small>Across all decisions</small></article>
            <article className="dw-stat-card"><span className="dw-stat-icon dw-stat-green"><Icon name="chart" /></span><span className="dw-stat-label">Your participation</span><strong>{Object.keys(myVotes).length}<span className="dw-stat-unit"> vote{Object.keys(myVotes).length === 1 ? '' : 's'}</span></strong><small>Thanks for weighing in</small></article>
          </section>

          <section className="dw-decisions-section" id="decisions">
            <div className="dw-section-heading">
              <div><span className="dw-eyebrow">MAKE YOUR VOICE COUNT</span><h2>Decisions for you</h2></div>
              <button className="dw-text-link" onClick={() => setActiveTab('all')} type="button">View all <Icon name="arrow" size={15} /></button>
            </div>
            <div className="dw-filter-row">
              <div aria-label="Filter decisions" className="dw-tabs" role="tablist">
                <button aria-selected={activeTab === 'all'} className={activeTab === 'all' ? 'is-active' : ''} onClick={() => setActiveTab('all')} role="tab" type="button">All decisions</button>
                <button aria-selected={activeTab === 'needs-vote'} className={activeTab === 'needs-vote' ? 'is-active' : ''} onClick={() => setActiveTab('needs-vote')} role="tab" type="button">Needs your vote <span className="dw-tab-count">{decisions.filter((decision) => !myVotes[decision.id]).length}</span></button>
                <button aria-selected={activeTab === 'my-votes'} className={activeTab === 'my-votes' ? 'is-active' : ''} onClick={() => setActiveTab('my-votes')} role="tab" type="button">Your votes</button>
              </div>
              <label className="dw-topic-select">
                <span className="dw-sr-only">Filter by topic</span>
                <select onChange={(event) => setCategory(event.target.value)} value={category}>
                  <option>All topics</option>
                  <option>Community</option>
                  <option>Product</option>
                  <option>Events</option>
                </select>
              </label>
            </div>
            <div className="dw-decision-list">
              {filteredDecisions.length ? filteredDecisions.map((decision) => (
                <DecisionCard
                  decision={decision}
                  key={decision.id}
                  myVote={myVotes[decision.id]}
                  onVote={castVote}
                />
              )) : (
                <div className="dw-empty-state">
                  <span className="dw-empty-icon"><Icon name="compass" size={23} /></span>
                  <strong>No decisions found</strong>
                  <p>Try a different filter, or start a new decision for your community.</p>
                  <button className="dw-text-link" onClick={() => {
                    setActiveTab('all')
                    setCategory('All topics')
                    setSearch('')
                  }} type="button">Clear filters <Icon name="arrow" size={15} /></button>
                </div>
              )}
            </div>
          </section>
          <footer className="dw-footer">Made for thoughtful decisions <span>✳</span></footer>
        </div>
      </main>
      {isModalOpen && <CreateDecisionModal onClose={() => setIsModalOpen(false)} onCreate={createDecision} />}
    </div>
  )
}
