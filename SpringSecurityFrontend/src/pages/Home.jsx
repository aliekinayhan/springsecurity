function Home() {
  return (
    <div className="max-w-3xl mx-auto mt-16 p-8">
      <div className="flex justify-between items-center mb-8">
        <h1 className="text-3xl font-bold">Ana Sayfa</h1>
        <button className="bg-red-500 text-white px-4 py-2 rounded hover:bg-red-600">
          Çıkış Yap
        </button>
      </div>
      <p className="text-gray-600 leading-relaxed">
        Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod
        tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim
        veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea
        commodo consequat.
      </p>
    </div>
  );
}

export default Home;
