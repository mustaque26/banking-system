import React, { useState } from "react";
import CustomerList from "./components/CustomerList.jsx";
import AccountList from "./components/AccountList.jsx";
import TransactionList from "./components/TransactionList.jsx";

export default function App() {
  const [selectedCustomer, setSelectedCustomer] = useState(null);
  const [selectedAccount, setSelectedAccount] = useState(null);

  return (
    <div className="layout">
      <CustomerList
        onSelect={c => {
          setSelectedCustomer(c);
          setSelectedAccount(null);
        }}
      />
      <AccountList
        customer={selectedCustomer}
        onSelect={a => setSelectedAccount(a)}
      />
      <TransactionList account={selectedAccount} />
    </div>
  );
}
