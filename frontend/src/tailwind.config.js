/** @type {import('tailwindcss').Config} */
module.exports = {
    content: ["./index.html", "./src/**/*.{js,jsx,ts,tsx}"],
    theme: {
        extend: {
            colors: {
                plum: "#3B0D45",     // Deep Plum
                burgundy: "#7A1F3D", // Burgundy
                coral: "#FF6B5B",    // Coral
                sunset: "#FF4F81",   // Sunset Pink
                orange: "#FF8C42",   // Orange accent
                ivory: "#FDF8F3",    // Soft Ivory background
            },
            fontFamily: {
                sans: ["Poppins", "ui-sans-serif", "system-ui", "sans-serif"],
            },
        },
    },
    plugins: [],
};