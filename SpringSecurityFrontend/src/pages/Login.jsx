function Login() {
  return (
    <div className="flex items-center justify-center h-screen bg-gray-100">
      <div className="bg-white p-8 rounded-lg shadow-md w-96">
        <h1 className="text-2xl font-bold mb-6 text-center">Giriş Yap</h1>
        <input
          type="text"
          placeholder="Kullanıcı adı"
          className="w-full border p-2 rounded mb-4"
        />
        <input
          type="password"
          placeholder="Şifre"
          className="w-full border p-2 rounded mb-6"
        />
        <button className="w-full bg-blue-600 text-white p-2 rounded hover:bg-blue-700">
          Giriş Yap
        </button>
      </div>
    </div>
  );
}

export default Login;
