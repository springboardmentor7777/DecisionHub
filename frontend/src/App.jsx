import { BrowserRouter, Routes, Route } from "react-router-dom";
import LoginPage from "./features/auth/LoginPage.jsx";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<LoginPage />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;