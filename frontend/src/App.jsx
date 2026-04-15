import { useEffect, useState } from 'react'

function App() {
  const [status, setStatus] = useState(null)

  useEffect(() => {
    fetch('http://localhost:8080/api/health')
      .then(res => res.text())
      .then(data => setStatus(data))
      .catch(err => setStatus('Error: ' + err.message))
  }, [])
  
  return <div>Backend status: {status}</div>
}

export default App