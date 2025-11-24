import React, { useEffect, useState } from "react";
import { fetchAudits } from "../api/client";

export default function AuditList({ entityType, entityId, onClose }) {
  const [entries, setEntries] = useState([]);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!entityType || !entityId) return;
    fetchAudits(entityType, entityId)
      .then(setEntries)
      .catch(e => setError(e.message || 'Failed to load audits'));
  }, [entityType, entityId]);

  return (
    <div className="card">
      <h3>Audit trail</h3>
      <button onClick={onClose}>Close</button>
      {error && <div style={{ color: 'red' }}>{error}</div>}
      <ul className="list">
        {entries.map(a => (
          <li key={a.id}>
            <div><strong>{a.timestamp}</strong> by {a.actor} — {a.action}</div>
            <div style={{ fontSize: '0.9em', color: '#444' }}>{a.details}</div>
          </li>
        ))}
      </ul>
    </div>
  );
}

