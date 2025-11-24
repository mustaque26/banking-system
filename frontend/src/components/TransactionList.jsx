import React, { useEffect, useState } from "react";
import client from "../api/client";

export default function TransactionList({ account }) {
  const [txns, setTxns] = useState([]);
  const [form, setForm] = useState({ type: "CREDIT", amount: 0, description: "" });
  const [error, setError] = useState(null);
  const [balance, setBalance] = useState(null);

  useEffect(() => {
    if (!account || account.id == null) return;
    setError(null);
    client
      .get(`/transactions/byAccount/${account.id}`)
      .then(res => setTxns(res.data))
      .catch(err => setError(err.message || 'Failed to load transactions'));
    // load balance
    client.get(`/accounts/${account.id}/balance`)
      .then(res => setBalance(res.data.balance))
      .catch(err => setError(err.message || 'Failed to load balance'));
  }, [account]);

  if (!account) return <div className="card">Select an account</div>;

  const refresh = () =>
    {
      if (!account || account.id == null) return Promise.resolve();
      return client.get(`/transactions/byAccount/${account.id}`)
        .then(res => setTxns(res.data))
        .catch(err => setError(err.message || 'Failed to refresh'));
    }

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!account || account.id == null) {
      setError('No account selected');
      return;
    }
    await client.post(`/transactions/post/${account.id}`, form);
    setForm({ ...form, amount: 0, description: "" });
    await refresh();
    // refresh balance after posting
    client.get(`/accounts/${account.id}/balance`)
      .then(res => setBalance(res.data.balance))
      .catch(err => setError(err.message || 'Failed to refresh balance'));
  };

  return (
    <div className="card">
      <h2>Transactions – {account.accountNumber}</h2>
      <div>Balance: {balance != null ? balance : '—'}</div>
      {error && <div style={{ color: 'red' }}>{error}</div>}
      <ul className="list">
        {txns.map(t => (
          <li key={t.id}>
            {t.txnTime} – {t.type} – {t.amount} – {t.description}
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
          value={form.amount}
          onChange={e => setForm({ ...form, amount: Number(e.target.value) })}
          placeholder="Amount"
        />
        <input
          value={form.description}
          onChange={e => setForm({ ...form, description: e.target.value })}
          placeholder="Description"
        />
        <button type="submit">Post Txn</button>
      </form>
    </div>
  );
}
