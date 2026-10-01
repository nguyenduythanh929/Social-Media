import { BrowserRouter as Router, Route, Routes } from "react-router-dom";
import Login from "../pages/Login";
import Home from "../pages/Home";
import Profile from "../pages/Profile";
import Chat from "../pages/Chat";
import Search from "../pages/Search";
import Register from "../pages/Register";
import UserProfile from "../pages/UserProfile";
import Friends from "../pages/Friends";

const AppRoutes = () => {
  return (
    <Router>
      <Routes>
        {" "}
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/" element={<Home />} />
        <Route path="/profile" element={<Profile />} />
        <Route path="/chat" element={<Chat />} />
        <Route path="/search" element={<Search />} />
        <Route path="/users/:userId" element={<UserProfile />} />
        <Route path="/friends" element={<Friends />} />
      </Routes>
    </Router>
  );
};

export default AppRoutes;
