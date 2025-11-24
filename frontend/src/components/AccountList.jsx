import React, { useEffect, useState } from "react";
import client from "../api/client";

export default function AccountList({ customer, onSelect }) {
  const [accounts, setAccounts] = useState([]);
  const [currency, setCurrency] = useState("INR");

  useEffect(() => {
    if (!customer) return;
    client
      .get(`/accounts/byCustomer/${customer.id}`)
      .then(res => setAccounts(res.data));
  }, [customer]);

  if (!customer) return <div className="card">Select a customer</div>;

  const handleCreate = async (e) => {
    e.preventDefault();
    const resp = await client.post(`/accounts/forCustomer/${customer.id}`, {
      accountNumber: `AC-${Date.now()}`,
      currency,
      status: "ACTIVE"
    });
    setAccounts([...accounts, resp.data]);
  };

  return (
    <div className="card">
      <h2>Accounts of {customer.fullName}</h2>
      <ul className="list">
        {accounts.map(a => (
          <li key={a.id} onClick={() => onSelect(a)}>
            {a.accountNumber} – {a.currency} – Bal: {a.balance}
          </li>
        ))}
      </ul>

      <form onSubmit={handleCreate} className="form">
        <select value={currency} onChange={e => setCurrency(e.target.value)}>
          <option value="INR">INR</option>
          <option value="USD">USD</option>
        </select>
        <button type="submit">Open Account</button>
      </form>
    </div>
  );
}
