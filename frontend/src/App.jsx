import PersonalDetailsForm from './features/personal-details/components/PersonalDetailsForm.jsx'
import './App.css'

function App() {
  return (
    <div className="app-shell">
      <aside className="sidebar" aria-label="Main navigation">
        <a className="brand" href="/" aria-label="PeopleDesk home">
          <span className="brand-mark" aria-hidden="true">P</span>
          <span>People<span className="brand-light">Desk</span></span>
        </a>

        <p className="nav-label">WORKSPACE</p>
        <nav>
          <a className="nav-item" href="#overview">
            <span className="nav-icon" aria-hidden="true">▦</span>
            Overview
          </a>
          <a className="nav-item" href="#employees">
            <span className="nav-icon" aria-hidden="true">♙</span>
            Employees
          </a>
          <a className="nav-item active" href="#personal-details" aria-current="page">
            <span className="nav-icon" aria-hidden="true">▤</span>
            Personal details
          </a>
          <a className="nav-item" href="#time-off">
            <span className="nav-icon" aria-hidden="true">◷</span>
            Time off
          </a>
          <a className="nav-item" href="#settings">
            <span className="nav-icon" aria-hidden="true">⚙</span>
            Settings
          </a>
        </nav>

        <div className="sidebar-bottom">
          <div className="sidebar-help-icon" aria-hidden="true">?</div>
          <div>
            <strong>Need a hand?</strong>
            <span>Visit the help center</span>
          </div>
          <span className="help-arrow" aria-hidden="true">↗</span>
        </div>
      </aside>

      <main className="main-content" id="personal-details">
        <header className="topbar">
          <div className="breadcrumb">Employees <span>/</span> My profile</div>
          <div className="topbar-user">
            <button type="button" className="icon-button" aria-label="Notifications">
              <span aria-hidden="true">♧</span>
              <i />
            </button>
            <div className="user-avatar" aria-hidden="true">JD</div>
            <div className="user-label">
              <strong>Jordan Davis</strong>
              <span>Employee</span>
            </div>
            <span className="user-chevron" aria-hidden="true">⌄</span>
          </div>
        </header>

        <div className="page-content">
          <div className="page-heading">
            <div>
              <div className="eyebrow">MY PROFILE <span>•</span> EMPLOYEE INFORMATION</div>
              <h1>Personal Details</h1>
              <p>Manage your personal information and emergency contact details.</p>
            </div>
            <div className="profile-progress" aria-label="Profile completion: 75 percent">
              <div className="progress-ring"><span>75%</span></div>
              <div><strong>Profile strength</strong><span>Almost there</span></div>
            </div>
          </div>

          <PersonalDetailsForm />

          <footer className="page-footer">
            <span>© 2025 PeopleDesk</span>
            <span>Your information is private and secure <span className="footer-lock" aria-hidden="true">▣</span></span>
          </footer>
        </div>
      </main>
    </div>
  )
}

export default App
