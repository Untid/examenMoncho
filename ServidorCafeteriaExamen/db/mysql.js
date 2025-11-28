// db/mysql.js
const mysql = require('mysql2/promise');

const pool = mysql.createPool({
  host: 'dam2.colexio-karbo.com',
  port: 3333,
  user: 'dam2',         // ajusta si usas otro usuario
  password: 'Ka3b0134679',         // ajusta tu contraseña
  database: 'karbo_jotero',
  waitForConnections: true,
  connectionLimit: 10
});

module.exports = pool;