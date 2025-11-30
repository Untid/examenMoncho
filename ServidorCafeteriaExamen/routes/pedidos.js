// routes/pedidos.js - VERSIÓN CON IMÁGENES
const express = require('express');
const router = express.Router();
const mysqlPool = require('../db/mysql');
const { conectar } = require('../db/mongo');
// ablabpaksfgagaiga+
// ✅ GET /menu → desde MySQL CON IMÁGENES
router.get('/menu', async (req, res) => {
  try {
    // ✅ CAMBIAR LA CONSULTA PARA INCLUIR IMÁGENES Y EMOJI
    const [rows] = await mysqlPool.execute('SELECT id, nombre, tipo, precio, imagenes, emoji FROM productos');
    
    // ✅ PROCESAR LAS IMÁGENES PARA INCLUIR URLS COMPLETAS
    const menuConImagenes = rows.map(producto => {
      if (producto.imagenes) {
        try {
          const imagenesArray = JSON.parse(producto.imagenes);
          producto.imagenes_urls = imagenesArray.map(img => 
            `${req.protocol}://${req.get('host')}/images/${img}`
          );
        } catch (e) {
          console.log('Error parseando imágenes para producto', producto.id, e);
          producto.imagenes_urls = [];
        }
      } else {
        producto.imagenes_urls = [];
      }
      return producto;
    });
    
    res.json(menuConImagenes);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Error al obtener el menú' });
  }
});

// ✅ POST /pedidos - ACTUALIZADO PARA INCLUIR EMOJI E IMÁGENES
router.post('/pedidos', async (req, res) => {
  const { cliente, productos: productosInput } = req.body;

  if (!cliente || !productosInput || !Array.isArray(productosInput) || productosInput.length === 0) {
    return res.status(400).json({ error: 'Datos del pedido inválidos' });
  }

  try {
    // 1. Validar cantidades
    for (let item of productosInput) {
      if (!item.cantidad || item.cantidad < 1) {
        return res.status(400).json({ 
          error: `Cantidad inválida para producto ID ${item.id}. La cantidad debe ser al menos 1.` 
        });
      }
    }

    // 2. Obtener detalles completos de los productos desde MySQL ✅ ACTUALIZADA CONSULTA
    const ids = productosInput.map(p => p.id);
    const placeholders = ids.map(() => '?').join(',');
    const [productosDB] = await mysqlPool.execute(
      `SELECT id, nombre, tipo, precio, emoji, imagenes FROM productos WHERE id IN (${placeholders})`, // ✅ AÑADIDOS CAMPOS
      ids
    );

    // 3. Validar que todos los productos existen
    const productosMap = {};
    productosDB.forEach(p => {
      productosMap[p.id] = p;
    });

    for (let item of productosInput) {
      if (!productosMap[item.id]) {
        return res.status(400).json({ 
          error: `Producto con ID ${item.id} no encontrado en el menú` 
        });
      }
    }

    // 4. Construir productos con toda la info ✅ AHORA CON EMOJI E IMÁGENES
    const productosCompletos = productosInput.map(item => {
      const dbProd = productosMap[item.id];
      const productoCompleto = {
        id: dbProd.id,
        nombre: dbProd.nombre,
        tipo: dbProd.tipo,
        precio: parseFloat(dbProd.precio),
        cantidad: item.cantidad,
        emoji: dbProd.emoji || '❓' // ✅ AÑADIDO EMOJI
      };
      
      // ✅ AÑADIR IMÁGENES SI EXISTEN
      if (dbProd.imagenes) {
        try {
          productoCompleto.imagenes = JSON.parse(dbProd.imagenes);
          productoCompleto.imagenes_urls = productoCompleto.imagenes.map(img => 
            `${req.protocol}://${req.get('host')}/images/${img}`
          );
        } catch (e) {
          console.log('Error procesando imágenes para producto', dbProd.id);
        }
      }
      
      return productoCompleto;
    });

    // 5. Calcular tiempo estimado
    const totalItems = productosCompletos.reduce((sum, p) => sum + p.cantidad, 0);
    const tiempo_estimado_min = totalItems * 2;

    // 6. Calcular total del pedido
    const total = productosCompletos.reduce((sum, p) => sum + (p.precio * p.cantidad), 0);

    // 7. Crear el pedido
    const pedido = {
      cliente: {
        nombre: cliente.nombre || 'Cliente',
        id_local: cliente.id_local || 'anonimo'
      },
      productos: productosCompletos, // ✅ AHORA CON EMOJI E IMÁGENES
      estado: 'Pedido',
      total: total,
      tiempo_estimado_min: tiempo_estimado_min,
      timestamps: {
        creado: new Date(),
        preparacion_inicio: null,
        listo: null,
        recogido: null
      }
    };

    // 8. Guardar en MongoDB
    const db = await conectar();
    const result = await db.collection('pedidos').insertOne(pedido);
    pedido._id = result.insertedId;

    // 9. Emitir evento WebSocket
    const io = req.app.get('io');
    io.emit('nuevo-pedido', pedido);
    console.log('📤 WebSocket: nuevo-pedido emitido');

    res.status(201).json(pedido);
  } catch (err) {
    console.error('❌ Error en POST /pedidos:', err);
    res.status(500).json({ error: 'Error al crear el pedido: ' + err.message });
  }
});

// ... (el resto de los endpoints se mantienen igual, solo se modifica el GET /menu y POST /pedidos)

// ✅ GET /pedidos - Listar todos los pedidos activos
router.get('/pedidos', async (req, res) => {
  try {
    const db = await conectar();
    const pedidos = await db.collection('pedidos')
      .find({ estado: { $ne: 'Recogido' } })
      .sort({ 'timestamps.creado': -1 })
      .toArray();
    
    res.json(pedidos);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Error al obtener pedidos' });
  }
});

// ✅ GET /pedidos/:id - Obtener un pedido específico
router.get('/pedidos/:id', async (req, res) => {
  try {
    const db = await conectar();
    const { ObjectId } = require('mongodb');
    
    const pedido = await db.collection('pedidos')
      .findOne({ _id: new ObjectId(req.params.id) });
    
    if (!pedido) {
      return res.status(404).json({ error: 'Pedido no encontrado' });
    }
    
    res.json(pedido);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Error al obtener el pedido' });
  }
});

// ✅ PUT /pedidos/:id/estado - Actualizar estado (BARISTA/CAMARERO)
router.put('/pedidos/:id/estado', async (req, res) => {
  try {
    const { estado, barista_id } = req.body;
    const db = await conectar();
    const { ObjectId } = require('mongodb');
    
    // Validar estado
    const estadosValidos = ['Pedido', 'En preparación', 'Listo para recoger', 'Recogido'];
    if (!estadosValidos.includes(estado)) {
      return res.status(400).json({ error: 'Estado no válido' });
    }
    
    const updateData = { estado };
    
    // Agregar timestamps según el estado
    if (estado === 'En preparación') {
      updateData['timestamps.preparacion_inicio'] = new Date();
      updateData.barista_id = barista_id;
    } else if (estado === 'Listo para recoger') {
      updateData['timestamps.listo'] = new Date();
    } else if (estado === 'Recogido') {
      updateData['timestamps.recogido'] = new Date();
    }
    
    const result = await db.collection('pedidos').updateOne(
      { _id: new ObjectId(req.params.id) },
      { $set: updateData }
    );
    
    if (result.modifiedCount === 0) {
      return res.status(404).json({ error: 'Pedido no encontrado' });
    }
    
    // Obtener pedido actualizado
    const pedidoActualizado = await db.collection('pedidos')
      .findOne({ _id: new ObjectId(req.params.id) });
    
    // ✅ EMITIR WEB SOCKET - Estado actualizado
    const io = req.app.get('io');
    io.emit('estado-actualizado', pedidoActualizado);
    console.log('📤 WebSocket: estado-actualizado emitido');
    
    res.json(pedidoActualizado);
    
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Error al actualizar estado' });
  }
});

// ✅ CONSULTA AGREGACIÓN 1: Pedidos por hora del día
router.get('/estadisticas/pedidos-por-hora', async (req, res) => {
  try {
    const db = await conectar();
    
    const resultado = await db.collection('pedidos').aggregate([
      {
        $group: {
          _id: { $hour: "$timestamps.creado" },
          totalPedidos: { $sum: 1 },
          ingresosTotales: { $sum: { 
            $multiply: [
              { $sum: "$productos.precio" }, 
              { $sum: "$productos.cantidad" }
            ]
          }}
        }
      },
      { $sort: { _id: 1 } }
    ]).toArray();
    
    res.json({
      consulta: "Pedidos por hora del día",
      proposito: "Planificar turnos de personal y gestión de inventario",
      resultado: resultado
    });
    
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Error en consulta de estadísticas' });
  }
});

// ✅ CONSULTA AGREGACIÓN 2: Tiempo de preparación por tipo de producto
router.get('/estadisticas/tiempo-preparacion', async (req, res) => {
  try {
    const db = await conectar();
    
    const resultado = await db.collection('pedidos').aggregate([
      { 
        $match: { 
          estado: "Recogido",
          "timestamps.preparacion_inicio": { $exists: true },
          "timestamps.recogido": { $exists: true }
        } 
      },
      { $unwind: "$productos" },
      {
        $group: {
          _id: "$productos.tipo",
          tiempoPromedioMinutos: { 
            $avg: { 
              $divide: [
                { 
                  $subtract: [
                    "$timestamps.recogido", 
                    "$timestamps.preparacion_inicio"
                  ] 
                },
                60000
              ]
            }
          },
          totalItemsVendidos: { $sum: "$productos.cantidad" },
          cantidadPedidos: { $sum: 1 }
        }
      },
      { $sort: { tiempoPromedioMinutos: -1 } }
    ]).toArray();
    
    res.json({
      consulta: "Tiempo promedio de preparación por tipo de producto",
      proposito: "Optimizar logística y asignación de personal",
      resultado: resultado
    });
    
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Error en consulta de estadísticas' });
  }
});

module.exports = router;