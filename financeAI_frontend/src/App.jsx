import { useEffect, useState } from 'react'
import { ApiError, authApi, financeApi, getToken, getUser } from './services/api'
import './App.css'

const capabilities = [
  { icon: '↗', title: 'Entiende tus movimientos', description: 'Registra tus gastos y deja que FinanceAI los organice para mostrarte en qué usas tu dinero.' },
  { icon: '◔', title: 'Visualiza tu salud financiera', description: 'Reúne ingresos, hábitos de ahorro y nivel de endeudamiento en una visión clara y personal.' },
  { icon: '✦', title: 'Recibe recomendaciones', description: 'Identifica oportunidades para cuidar tu presupuesto y tomar mejores decisiones financieras.' },
]

function Home({ navigate }) {
  return (
    <div className="app-shell">
      <header className="topbar">
        <button className="brand button-reset" onClick={() => navigate('home')} aria-label="FinanceAI, inicio"><span className="brand-mark" aria-hidden="true">F</span><span>FinanceAI</span></button>
        <nav aria-label="Navegación principal"><a className="nav-link active" href="#inicio">Inicio</a><a className="nav-link" href="#como-funciona">Cómo funciona</a></nav>
        <div className="topbar-actions"><button className="header-register" onClick={() => navigate('register')}>Crear perfil</button><button className="login-link" onClick={() => navigate('login')}>Iniciar sesión <span aria-hidden="true">→</span></button></div>
      </header>

      <main id="inicio">
        <section className="hero-section" aria-labelledby="hero-title">
          <div className="hero-copy">
            <p className="eyebrow"><span className="eyebrow-dot" /> Tu bienestar financiero, en perspectiva</p>
            <h1 id="hero-title">Entiende tu dinero.<br /><em>Decide con confianza.</em></h1>
            <p className="hero-description">FinanceAI analiza tus movimientos, ingresos y hábitos para brindarte una visión completa y sencilla de tu salud financiera.</p>
            <div className="hero-actions"><button className="button button-primary" onClick={() => navigate('register')}>Comenzar ahora <span aria-hidden="true">→</span></button><a className="button button-secondary" href="#como-funciona">Conocer más</a></div>
            <p className="hero-note"><span aria-hidden="true">✦</span> Tu información está pensada para trabajar a tu favor.</p>
          </div>

          <div className="insight-card" aria-label="Ejemplo de resumen de salud financiera">
            <div className="card-heading"><div><p>Tu panorama este mes</p><strong>Salud financiera</strong></div><span className="status-pill">En progreso</span></div>
            <div className="score-row"><div className="score-ring"><span>76</span><small>/100</small></div><div><p className="score-label">Vas por buen camino</p><p className="score-text">Con algunos ajustes, tu presupuesto puede rendir aún más.</p></div></div>
            <div className="progress-label"><span>Distribución de gastos</span><span>Este mes</span></div>
            <div className="bar-group" aria-hidden="true">
              <div><span className="bar-name">Necesidades</span><span className="bar-track"><span className="bar-fill necessities" /></span><b>48%</b></div>
              <div><span className="bar-name">Estilo de vida</span><span className="bar-track"><span className="bar-fill lifestyle" /></span><b>29%</b></div>
              <div><span className="bar-name">Ahorro</span><span className="bar-track"><span className="bar-fill saving" /></span><b>23%</b></div>
            </div>
            <div className="tip"><span aria-hidden="true">✦</span><p><b>Una idea para ti</b>Registrar tus transacciones es el primer paso para descubrir tus oportunidades.</p></div>
          </div>
        </section>

        <section className="intro-section" id="como-funciona" aria-labelledby="intro-title">
          <div className="section-heading"><p className="eyebrow">Una mirada más completa</p><h2 id="intro-title">Las finanzas personales no son solo números.</h2><p>Son hábitos, decisiones y metas. FinanceAI conecta esa información para que puedas comprender tu situación actual y avanzar con más claridad.</p></div>
          <div className="capability-grid">{capabilities.map((capability) => <article className="capability-card" key={capability.title}><span className="capability-icon" aria-hidden="true">{capability.icon}</span><h3>{capability.title}</h3><p>{capability.description}</p></article>)}</div>
        </section>

        <section className="closing-section" id="acceder"><p className="eyebrow">Un paso a la vez</p><h2>Haz que cada decisión cuente.</h2><button className="button button-primary" onClick={() => navigate('register')}>Crear mi perfil <span aria-hidden="true">→</span></button></section>
      </main>
      <footer>© {new Date().getFullYear()} FinanceAI <span>•</span> Tu información, tus decisiones.</footer>
    </div>
  )
}

function AuthPage({ mode, navigate }) {
  const isLogin = mode === 'login'
  const [form, setForm] = useState({ nombre: '', apellido: '', email: '', contrasena: '' })
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  const updateField = (event) => setForm((current) => ({ ...current, [event.target.name]: event.target.value }))

  async function submit(event) {
    event.preventDefault()
    setError('')
    setSuccess('')
    setIsSubmitting(true)
    try {
      const response = isLogin
        ? await authApi.login({ email: form.email, contrasena: form.contrasena })
        : await authApi.register(form)
      setSuccess(`¡Hola, ${response.nombre}! Tu sesión ya está lista.`)
      navigate('dashboard', response)
    } catch (requestError) {
      setError(requestError instanceof ApiError ? requestError.message : 'No pudimos conectar con el servidor. Intenta nuevamente.')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <main className="auth-page">
      <header className="auth-header"><button className="brand button-reset" onClick={() => navigate('home')}><span className="brand-mark" aria-hidden="true">F</span><span>FinanceAI</span></button><button className="back-link button-reset" onClick={() => navigate('home')}>← Volver al inicio</button></header>
      <section className="auth-content">
        <div className="auth-intro"><p className="eyebrow"><span className="eyebrow-dot" /> Finanzas más claras, decisiones más tranquilas</p><h1>{isLogin ? <>Qué bueno verte<br /><em>otra vez.</em></> : <>Empieza a entender<br /><em>tu dinero.</em></>}</h1><p>{isLogin ? 'Ingresa para retomar el control de tu salud financiera.' : 'Crea tu perfil y comienza a transformar tus movimientos en información útil.'}</p></div>
        <form className="auth-card" onSubmit={submit}>
          <div className="auth-card-heading"><p>{isLogin ? 'Acceso a tu cuenta' : 'Crea tu cuenta'}</p><h2>{isLogin ? 'Iniciar sesión' : 'Crear perfil'}</h2></div>
          {!isLogin && <div className="name-fields"><label>Nombre<input name="nombre" value={form.nombre} onChange={updateField} required autoComplete="given-name" /></label><label>Apellido<input name="apellido" value={form.apellido} onChange={updateField} required autoComplete="family-name" /></label></div>}
          <label>Correo electrónico<input name="email" type="email" value={form.email} onChange={updateField} required autoComplete="email" placeholder="nombre@correo.com" /></label>
          <label>Contraseña<input name="contrasena" type="password" value={form.contrasena} onChange={updateField} required minLength="6" autoComplete={isLogin ? 'current-password' : 'new-password'} placeholder="Mínimo 6 caracteres" /></label>
          {!isLogin && <p className="password-note">Debe contener al menos 6 caracteres.</p>}
          {error && <p className="form-message error" role="alert">{error}</p>}
          {success && <p className="form-message success" role="status">{success}</p>}
          <button className="button button-primary auth-submit" type="submit" disabled={isSubmitting}>{isSubmitting ? 'Procesando…' : isLogin ? 'Ingresar a FinanceAI' : 'Crear mi perfil'} <span aria-hidden="true">→</span></button>
          <p className="auth-switch">{isLogin ? '¿Aún no tienes una cuenta?' : '¿Ya tienes una cuenta?'} <button type="button" className="text-button" onClick={() => navigate(isLogin ? 'register' : 'login')}>{isLogin ? 'Crear perfil' : 'Iniciar sesión'}</button></p>
        </form>
      </section>
    </main>
  )
}

function formatMoney(value) {
  return new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS', maximumFractionDigits: 0 }).format(Number(value) || 0)
}

const profileLabels = {
  SALUDABLE: 'Saludable',
  EN_OBSERVACION: 'En observación',
  EN_RIESGO: 'En riesgo',
}

function Dashboard({ user, navigate }) {
  const [analysis, setAnalysis] = useState(null)
  const [income, setIncome] = useState(null)
  const [isLoading, setIsLoading] = useState(true)
  const [incomeValue, setIncomeValue] = useState('')
  const [incomeError, setIncomeError] = useState('')
  const [isSavingIncome, setIsSavingIncome] = useState(false)
  const [isEditingIncome, setIsEditingIncome] = useState(false)
  const [transactionForm, setTransactionForm] = useState({ descripcion: '', valor: '' })
  const [transactions, setTransactions] = useState(null)
  const [categories, setCategories] = useState([])
  const [editingTransactionId, setEditingTransactionId] = useState(null)
  const [isAdjustmentMode, setIsAdjustmentMode] = useState(false)
  const [updatingCategoryId, setUpdatingCategoryId] = useState(null)
  const [categoryError, setCategoryError] = useState('')
  const [transactionError, setTransactionError] = useState('')
  const [isSavingTransaction, setIsSavingTransaction] = useState(false)

  useEffect(() => {
    let active = true
    financeApi.getMonthlyIncomeStatus()
      .then(async (response) => {
        if (!active) return
        setIncome(response)
        if (!response.registrado) return
        try {
          const analysisResponse = await financeApi.getAnalysis()
          if (active) setAnalysis(analysisResponse)
          const [transactionsResponse, categoriesResponse] = await Promise.all([financeApi.getTransactions(), financeApi.getCategories()])
          if (active) {
            setTransactions(transactionsResponse)
            setCategories(categoriesResponse)
          }
        } catch {
          if (active) setAnalysis(null)
        }
      })
      .catch(() => active && setIncome({ registrado: false }))
      .finally(() => active && setIsLoading(false))
    return () => { active = false }
  }, [])

  const fullName = `${user?.nombre || 'Usuario'} ${user?.apellido || ''}`.trim()
  const expenses = Object.entries(analysis?.gastosPorCategoria || {})
  const percentages = analysis?.porcentajePorCategoria || {}
  const totalExpenses = expenses.reduce((total, [, amount]) => total + Number(amount || 0), 0)
  const profile = analysis?.perfilFinanciero
  const recommendations = analysis?.recomendaciones || []

  async function logout() {
    try {
      await authApi.logout()
    } finally {
      navigate('home')
    }
  }

  async function saveMonthlyIncome(event) {
    event.preventDefault()
    setIncomeError('')
    const ingresoMensual = Number(incomeValue.replace(',', '.'))
    if (!Number.isFinite(ingresoMensual) || ingresoMensual <= 0) {
      setIncomeError('Ingresa un monto mayor a cero.')
      return
    }

    setIsSavingIncome(true)
    try {
      const response = income?.registrado
        ? await financeApi.updateMonthlyIncome({ ingresoMensual })
        : await financeApi.createMonthlyIncome({ ingresoMensual })
      setIncome({ registrado: true, ingresoMensual: response.ingresoMensual })
      setIncomeValue('')
      setIsEditingIncome(false)
      setAnalysis(await financeApi.getAnalysis())
      setTransactions(await financeApi.getTransactions())
    } catch (requestError) {
      setIncomeError(requestError instanceof ApiError ? requestError.message : 'No pudimos guardar tu ingreso. Intenta nuevamente.')
    } finally {
      setIsSavingIncome(false)
    }
  }

  async function loadTransactions(page) {
    try {
      setTransactions(await financeApi.getTransactions(page))
    } catch {
      setTransactions(null)
    }
  }

  async function updateTransactionCategory(transaction, event) {
    const categoriaId = Number(event.target.value)
    if (!categoriaId || categoriaId === transaction.categoriaId) {
      setEditingTransactionId(null)
      return
    }

    setCategoryError('')
    setUpdatingCategoryId(transaction.idTransaccion)
    try {
      await financeApi.updateTransactionCategory(transaction.idTransaccion, categoriaId)
      const [analysisResponse, transactionsResponse] = await Promise.all([financeApi.getAnalysis(), financeApi.getTransactions(transactions?.number || 0)])
      setAnalysis(analysisResponse)
      setTransactions(transactionsResponse)
      setEditingTransactionId(null)
    } catch (requestError) {
      setCategoryError(requestError instanceof ApiError ? requestError.message : 'No pudimos actualizar la categoría.')
    } finally {
      setUpdatingCategoryId(null)
    }
  }

  function toggleAdjustmentMode() {
    setIsAdjustmentMode((current) => !current)
    setEditingTransactionId(null)
    setCategoryError('')
  }

  async function saveTransaction(event) {
    event.preventDefault()
    setTransactionError('')
    const valor = Number(transactionForm.valor.replace(',', '.'))
    if (!transactionForm.descripcion.trim() || !Number.isFinite(valor) || valor <= 0) {
      setTransactionError('Completa una descripción y un monto mayor a cero.')
      return
    }
    setIsSavingTransaction(true)
    try {
      await financeApi.createTransaction({ descripcion: transactionForm.descripcion.trim(), valor })
      setTransactionForm({ descripcion: '', valor: '' })
      const [analysisResponse, transactionsResponse] = await Promise.all([financeApi.getAnalysis(), financeApi.getTransactions()])
      setAnalysis(analysisResponse)
      setTransactions(transactionsResponse)
    } catch (requestError) {
      setTransactionError(requestError instanceof ApiError ? requestError.message : 'No pudimos registrar la transacción. Intenta nuevamente.')
    } finally {
      setIsSavingTransaction(false)
    }
  }

  return (
    <div className="dashboard-page">
      <header className="dashboard-header">
        <button className="brand button-reset" onClick={() => navigate('dashboard')}><span className="brand-mark" aria-hidden="true">F</span><span>FinanceAI</span></button>
        <div className="dashboard-user"><span className="user-avatar" aria-hidden="true">{(user?.nombre || 'U').charAt(0).toUpperCase()}</span><span>{fullName}</span><button className="logout-button" onClick={logout}>Cerrar sesión</button></div>
      </header>
      <main className="dashboard-content">
        <section className="dashboard-welcome">
          <div><p className="eyebrow"><span className="eyebrow-dot" /> Tu espacio financiero</p><h1>Hola, {fullName}.<br /><em>Qué bueno tenerte aquí.</em></h1><p>Este es tu panorama financiero. Registra tus movimientos para recibir una visión cada vez más clara y personal.</p></div>
          <div className="welcome-date"><span>Resumen actual</span><strong>{analysis?.mesYFecha || 'Tu perfil está listo'}</strong></div>
        </section>
        {!income?.registrado ? <section className="income-gate" aria-labelledby="income-title"><div><p className="eyebrow"><span className="eyebrow-dot" /> Primer paso</p><h2 id="income-title">Registra tu ingreso mensual</h2><p>Lo necesitamos para calcular tu dinero disponible, organizar tus gastos y desbloquear el resto de tu dashboard.</p></div><form onSubmit={saveMonthlyIncome}><label>¿Cuál es tu ingreso mensual?<input value={incomeValue} onChange={(event) => setIncomeValue(event.target.value)} inputMode="decimal" placeholder="Ej. 850000" autoFocus required disabled={isLoading} /></label>{incomeError && <p className="form-message error" role="alert">{incomeError}</p>}<button className="button button-primary" disabled={isLoading || isSavingIncome}>{isLoading ? 'Verificando…' : isSavingIncome ? 'Guardando…' : 'Guardar y continuar'} <span aria-hidden="true">→</span></button></form></section> : <>
        {income?.registrado && <section className="income-summary"><div><span>Ingreso mensual</span><strong>{formatMoney(income.ingresoMensual)}</strong></div><button className="text-button" onClick={() => { setIsEditingIncome((current) => !current); setIncomeError('') }}> {isEditingIncome ? 'Cancelar' : 'Actualizar ingreso'} </button>{isEditingIncome && <form onSubmit={saveMonthlyIncome}><input value={incomeValue} onChange={(event) => setIncomeValue(event.target.value)} inputMode="decimal" placeholder="Nuevo ingreso mensual" required /><button type="submit" disabled={isSavingIncome}>{isSavingIncome ? 'Guardando…' : 'Guardar'}</button>{incomeError && <p className="form-message error" role="alert">{incomeError}</p>}</form>}</section>}
        <section className="dashboard-grid" aria-label="Resumen financiero">
          <article className="dashboard-card highlight-card"><p>Dinero disponible</p><strong>{isLoading ? 'Cargando…' : formatMoney(analysis?.montoRestante)}</strong><span>{analysis ? 'Luego de tus movimientos registrados' : 'Registra ingresos y gastos para verlo aquí'}</span></article>
          <article className="dashboard-card"><p>Gastos registrados</p><strong>{isLoading ? 'Cargando…' : formatMoney(totalExpenses)}</strong><span>{expenses.length ? `${expenses.length} ${expenses.length === 1 ? 'categoría' : 'categorías'} con movimientos` : 'Aún no registraste gastos'}</span></article>
          <article className="dashboard-card tip-card"><span className="tip-symbol" aria-hidden="true">✦</span><p>Una idea para ti</p><strong>{expenses.length ? 'Revisa tus categorías con mayor peso para encontrar oportunidades de ahorro.' : 'Tu primer movimiento te ayudará a construir un panorama personalizado.'}</strong></article>
        </section>
        {analysis && <section className="financial-insights" aria-label="Estado y recomendaciones financieras">
          <article className={`profile-status profile-${profile?.toLowerCase().replace('_', '-') || 'unknown'}`}>
            <p className="eyebrow">Estado de tu perfil</p>
            <div className="profile-status-main"><span className="profile-icon" aria-hidden="true">{profile === 'SALUDABLE' ? '✓' : '!'}</span><div><h2>{profileLabels[profile] || 'Sin evaluar'}</h2><p>{profile === 'SALUDABLE' ? 'Tus gastos actuales se mantienen dentro de parámetros recomendados.' : profile === 'EN_RIESGO' ? 'Tus gastos necesitan atención para cuidar tu estabilidad financiera.' : 'Hay oportunidades para ajustar tu presupuesto y fortalecer tu perfil.'}</p></div></div>
          </article>
          <article className="recommendations-card">
            <div className="panel-heading"><div><p className="eyebrow">Análisis personalizado</p><h2>Recomendaciones para ti</h2></div><span className="recommendation-count">{recommendations.length}</span></div>
            <ul className="recommendation-list">{recommendations.map((recommendation, index) => <li key={`${recommendation}-${index}`}><span aria-hidden="true">✦</span><p>{recommendation}</p></li>)}</ul>
          </article>
        </section>}
        <section className="dashboard-panel"><div className="panel-heading"><div><p className="eyebrow">Tus movimientos</p><h2>Distribución de gastos</h2></div><span>{analysis?.mesYFecha || 'Sin datos todavía'}</span></div>{isLoading ? <p className="empty-state">Preparando tu resumen…</p> : expenses.length ? <div className="expense-list">{expenses.map(([category, amount]) => <div className="expense-row" key={category}><span>{category}</span><span className="expense-track"><i style={{ width: `${Math.min(Number(percentages[category] || 0), 100)}%` }} /></span><b>{formatMoney(amount)}</b><small>{Number(percentages[category] || 0).toFixed(0)}%</small></div>)}</div> : <div className="empty-state"><strong>Tu dashboard está listo para empezar.</strong><span>Cuando agregues transacciones, vas a ver aquí cómo se distribuye tu dinero.</span></div>}</section>
        <section className="transactions-section" aria-labelledby="transactions-title"><div className="transaction-form-card"><p className="eyebrow"><span className="eyebrow-dot" /> Nuevo movimiento</p><h2>Registra una transacción</h2><p>Describe el gasto y FinanceAI lo clasificará automáticamente.</p><form onSubmit={saveTransaction}><label>Descripción<input value={transactionForm.descripcion} onChange={(event) => setTransactionForm((current) => ({ ...current, descripcion: event.target.value }))} placeholder="Ej. Compra en supermercado" maxLength="500" required /></label><label>Monto<input value={transactionForm.valor} onChange={(event) => setTransactionForm((current) => ({ ...current, valor: event.target.value }))} inputMode="decimal" placeholder="Ej. 24500" required /></label>{transactionError && <p className="form-message error" role="alert">{transactionError}</p>}<button className="button button-primary" disabled={isSavingTransaction}>{isSavingTransaction ? 'Registrando…' : 'Registrar transacción'} <span aria-hidden="true">→</span></button></form></div><div className={`transaction-list-card ${isAdjustmentMode ? 'adjustment-mode' : ''}`}><div className="panel-heading"><div><p className="eyebrow">Historial</p><h2 id="transactions-title">Tus transacciones</h2></div><div className="transaction-heading-actions"><span>{transactions?.totalElements || 0} registradas</span><button className="adjust-button" type="button" onClick={toggleAdjustmentMode} aria-pressed={isAdjustmentMode}><span aria-hidden="true">⚙</span> {isAdjustmentMode ? 'Terminar ajuste' : 'Ajustar'}</button></div></div>{isAdjustmentMode && <p className="adjustment-note">Elegí el lápiz junto a una categoría para corregirla. Cuando termines, seleccioná “Terminar ajuste”.</p>}{categoryError && <p className="form-message error" role="alert">{categoryError}</p>}{transactions?.content?.length ? <><div className="transaction-list">{transactions.content.map((transaction) => <article className="transaction-item" key={transaction.idTransaccion}><div className="category-editor">{isAdjustmentMode && editingTransactionId === transaction.idTransaccion ? <select aria-label={`Categoría para ${transaction.descripcion}`} value={transaction.categoriaId} onChange={(event) => updateTransactionCategory(transaction, event)} disabled={updatingCategoryId === transaction.idTransaccion}><option value={transaction.categoriaId}>{updatingCategoryId === transaction.idTransaccion ? 'Actualizando…' : transaction.categoriaNombre}</option>{categories.filter((category) => category.id !== transaction.categoriaId).map((category) => <option key={category.id} value={category.id}>{category.nombre}</option>)}</select> : <><span className="transaction-category">{transaction.categoriaNombre}</span>{isAdjustmentMode && <button className="edit-category-button" type="button" onClick={() => { setCategoryError(''); setEditingTransactionId(transaction.idTransaccion) }} aria-label={`Cambiar categoría de ${transaction.descripcion}`} title="Cambiar categoría">✎</button>}</>}</div><div><strong>{transaction.descripcion}</strong><small>{transaction.fecha}</small></div><b>{formatMoney(transaction.valor)}</b></article>)}</div>{transactions.totalPages > 1 && <div className="pagination"><button onClick={() => loadTransactions(transactions.number - 1)} disabled={transactions.first}>← Anterior</button><span>Página {transactions.number + 1} de {transactions.totalPages}</span><button onClick={() => loadTransactions(transactions.number + 1)} disabled={transactions.last}>Siguiente →</button></div>}</> : <div className="empty-state"><strong>Aún no hay transacciones.</strong><span>Registra la primera para comenzar a ver tu historial.</span></div>}</div></section>
        </>}
      </main>
    </div>
  )
}

function App() {
  const [view, setView] = useState('home')
  const [user, setCurrentUser] = useState(() => getUser())

  const navigate = (nextView, authenticatedUser) => {
    if (authenticatedUser) setCurrentUser(authenticatedUser)
    setView(nextView)
  }

  if (view === 'login' || view === 'register') return <AuthPage mode={view} navigate={navigate} />
  if (view === 'dashboard' && user) return <Dashboard user={user} navigate={navigate} />
  if (view === 'dashboard' && getToken()) return <Dashboard user={getUser()} navigate={navigate} />
  return <Home navigate={navigate} />
}

export default App
