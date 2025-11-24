import React, { useEffect, useState } from "react";
import client, { fetchAudits } from "../api/client";
import AuditList from "./AuditList";

export default function TransactionList({ account }) {
  const [txns, setTxns] = useState([]);
  const [form, setForm] = useState({ type: "CREDIT", amount: "0", description: "" });
  const [error, setError] = useState(null);
  const [balance, setBalance] = useState(null);
  const [showAudits, setShowAudits] = useState(false);
  const [auditEntity, setAuditEntity] = useState({ entityType: null, entityId: null });

  useEffect(() => {
    if (!account || account.id == null) return;
    loadAll();
  }, [account]);

  const loadAll = () => {
    setError(null);
    client
      .get(`/transactions/byAccount/${account.id}`)
      .then(res => setTxns(res.data))
      .catch(err => setError(err.message || 'Failed to load transactions'));
    // load balance
    client.get(`/accounts/${account.id}/balance`)
      .then(res => setBalance(res.data.balance))
      .catch(err => setError(err.message || 'Failed to load balance'));
  }

  if (!account) return <div className="card">Select an account</div>;

  const refresh = () => loadAll();

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!account || account.id == null) {
      setError('No account selected');
      return;
    }
    try {
      await client.post(`/transactions/post/${account.id}`, { ...form, amount: form.amount.toString() });
      setForm({ ...form, amount: "0", description: "" });
      await refresh();
      alert('Transaction created and is pending approval');
    } catch (err) {
      setError(err.message || 'Failed to post transaction');
    }
  };

  const approveTxn = async (txnId) => {
    if (!confirm('Approve transaction #' + txnId + '?')) return;
    try {
      await client.post(`/transactions/approve/${txnId}`);
      await refresh();
      alert('Transaction approved and posted');
    } catch (err) {
      setError(err.message || 'Failed to approve');
    }
  }

  const rejectTxn = async (txnId) => {
    const reason = prompt('Reason for rejection (optional):');
    if (!confirm('Reject transaction #' + txnId + '?')) return;
    try {
      await client.post(`/transactions/reject/${txnId}`, reason || '');
      await refresh();
      alert('Transaction rejected');
    } catch (err) {
      setError(err.message || 'Failed to reject');
    }
  }

  const openTxnAudits = (txnId) => {
    setAuditEntity({ entityType: 'TRANSACTION', entityId: String(txnId) });
    setShowAudits(true);
  }

  return (
    <div className="card">
      <h2>Transactions – {account.accountNumber}</h2>
      <div>Balance: {balance != null ? balance : '—'}</div>
      {error && <div style={{ color: 'red' }}>{error}</div>}

      <div style={{ marginBottom: 8 }}>
        <button onClick={() => { setAuditEntity({ entityType: 'ACCOUNT', entityId: String(account.id) }); setShowAudits(s => !s); }}>{showAudits ? 'Hide' : 'Show'} Audit Trail</button>
      </div>

      {showAudits && auditEntity.entityType && <AuditList entityType={auditEntity.entityType} entityId={auditEntity.entityId} onClose={() => setShowAudits(false)} />}

      <ul className="list">
        {txns.map(t => (
          <li key={t.id} style={{ marginBottom: 8 }}>
            <div>{t.txnTime} – {t.type} – {t.amount} – {t.description}</div>
            <div>Status: <strong>{t.status}</strong> {t.suspicious ? <span style={{color:'orange'}}> (Suspicious)</span> : null} {t.complianceFlag ? <span style={{color:'purple'}}> [{t.complianceFlag}]</span> : null}</div>
            <div style={{ marginTop: 6 }}>
              {t.status === 'PENDING' && (
                <>
                  <button onClick={() => approveTxn(t.id)}>Approve</button>
                  <button onClick={() => rejectTxn(t.id)} style={{ marginLeft: 8 }}>Reject</button>
                </>
              )}
              <button onClick={() => openTxnAudits(t.id)} style={{ marginLeft: 8 }}>Show Txn Audit</button>
            </div>
          </li>
        ))}
      </ul>

      <form onSubmit={handleSubmit} className="form">
        <select
          value={form.type}
          onChange={e => setForm({ ...form, type: e.target.value })}
        >
          <option value="CREDIT">Credit</option>
          <option value="DEBIT">Debit</option>
        </select>
        <input
          type="number"
          step="0.01"
          value={form.amount}
          onChange={e => setForm({ ...form, amount: e.target.value })}
          placeholder="Amount"
        />
        <input
          value={form.description}
          onChange={e => setForm({ ...form, description: e.target.value })}
          placeholder="Description"
        />
        <button type="submit">Create Transaction</button>
      </form>
    </div>
  );
}
