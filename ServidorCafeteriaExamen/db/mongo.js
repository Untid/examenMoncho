// db/mongo.js
const { MongoClient, ObjectId } = require('mongodb');

// Si usas MongoDB no local:
const uri = 'mongodb://admin:Ka3b0134679@dam2.colexio-karbo.com:57017/jotero?authSource=admin';

const client = new MongoClient(uri);

async function conectar() {
  await client.connect();
  console.log('✅ Conectado a MongoDB');
  return client.db('jotero'); // Nombre de tu BD en MongoDB
}

module.exports = { conectar };