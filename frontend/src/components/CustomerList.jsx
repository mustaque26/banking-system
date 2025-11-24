import React, { useEffect, useState } from "react";
import client from "../api/client";

export default function CustomerList({ onSelect }) {
  const [customers, setCustomers] = useState([]);
  const [form, setForm] = useState({ fullName: "", email: "", mobile: "" });

  useEffect(() => {
    client.get("/customers").then(res => setCustomers(res.data));
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    const resp = await client.post("/customers", {
      ...form,
      customerNumber: `CUST-${Date.now()}`
    });
    setCustomers([...customers, resp.data]);
    setForm({ fullName: "", email: "", mobile: "" });
  };

  return (
    <div className="card">
      <h2>Customers</h2>
      <ul className="list">
        {customers.map(c => (
          <li key={c.id} onClick={() => onSelect(c)}>
            {c.fullName} ({c.customerNumber})
          </li>
        ))}
      </ul>

      <form onSubmit={handleSubmit} className="form">
        <input
          placeholder="Full name"
          value={form.fullName}
          onChange={e => setForm({ ...form, fullName: e.target.value })}
        />
        <input
          placeholder="Email"
          value={form.email}
          onChange={e => setForm({ ...form, email: e.target.value })}
        />
        <input
          placeholder="Mobile"
          value={form.mobile}
          onChange={e => setForm({ ...form, mobile: e.target.value })}
        />
        <button type="submit">Add Customer</button>
      </form>
    </div>
  );
}
