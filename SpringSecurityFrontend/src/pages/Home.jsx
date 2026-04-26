import { useNavigate, Link } from "react-router-dom";
import { apiFetch } from "../api";

function Home() {
  const navigate = useNavigate();

  const handleLogout = async () => {
    const refreshToken = localStorage.getItem("refreshToken");
    await apiFetch("/auth/logout", {
      method: "POST",
      body: JSON.stringify({ refreshToken }),
    });
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    navigate("/login");
  };

  return (
    <div className="max-w-3xl mx-auto mt-16 p-8">
      <div className="flex justify-between items-center mb-8">
        <div className="flex gap-6">
          <Link to="/" className="font-bold text-blue-600">
            Ana Sayfa
          </Link>
          <Link to="/profile" className="text-gray-600 hover:text-blue-600">
            Profil
          </Link>
          <Link to="/about" className="text-gray-600 hover:text-blue-600">
            Hakkında
          </Link>
          <Link to="/settings" className="text-gray-600 hover:text-blue-600">
            Ayarlar
          </Link>
        </div>
        <button
          onClick={handleLogout}
          className="bg-red-500 text-white px-4 py-2 rounded hover:bg-red-600"
        >
          Çıkış Yap
        </button>
      </div>
      <p className="text-gray-600 leading-relaxed">
        Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod
        tempor incididunt ut labore et dolore magna aliqua.
      </p>
    </div>
  );
}

export default Home;
