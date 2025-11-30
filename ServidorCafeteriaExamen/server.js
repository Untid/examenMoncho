// server.js - VERSIÓN CON IMÁGENES
const express = require('express');
const http = require('http');
const socketIo = require('socket.io');
const cors = require('cors');
const pedidosRouter = require('./routes/pedidos');
const path = require('path'); // ✅ AÑADIR ESTO

const app = express();
const server = http.createServer(app);
const io = socketIo(server, {
  cors: {
    origin: "*",
    methods: ["GET", "POST", "PUT"]
  }
});


// Hola buenos dias alblaba
// Middleware
app.use(cors());
app.use(express.json());

// ✅ SERVIR IMÁGENES ESTÁTICAS - AÑADIR ESTO
app.use('/images', express.static(path.join(__dirname, 'images')));

// ✅ Socket.IO
app.set('io', io);

io.on('connection', (socket) => {
  console.log('🔌 Cliente conectado:', socket.id);
  
  socket.on('cliente-conectado', (data) => {
    console.log('📱 Cliente app conectado:', data.id_local);
    
    socket.emit('test-conexion', {
      message: '✅ Conexión exitosa con Starbucks Server',
      clienteId: data.id_local,
      timestamp: new Date().toISOString(),
      status: 'connected'
    });
    
    console.log('🎉 Enviado evento test-conexion al cliente');
  });
});

// Rutas
app.use('/api', pedidosRouter);

// Puerto
const PORT = process.env.PORT || 3000;
server.listen(PORT, () => {
  console.log(`🚀 Servidor corriendo en http://localhost:${PORT}`);
  console.log(`📡 Endpoints disponibles:`);
  console.log(`   GET  /api/menu`);
  console.log(`   GET  /api/pedidos`);
  console.log(`   POST /api/pedidos`);
  console.log(`   PUT  /api/pedidos/:id/estado`);
  console.log(`   GET  /api/estadisticas/pedidos-por-hora`);
  console.log(`   GET  /api/estadisticas/tiempo-preparacion`);
  console.log(`   📸 GET  /images/nombre_archivo.png`); // ✅ NUEVO
});