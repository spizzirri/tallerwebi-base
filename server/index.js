const express = require('express');
const app = express();
const PORT = process.env.PORT || 3000;

app.use(express.json());

let objetos = [
	{ id: 1, nombre: 'Objeto 1' },
	{ id: 2, nombre: 'Objeto 2' }
];

const APIKEY = 'tallerwebi';

function validarApiKey(req, res, next) {
	const apikey = req.headers['apikey'];
	if (apikey !== APIKEY) {
		return res.status(401).json({ error: 'APIKEY inválida' });
	}
	next();
}

app.use(validarApiKey);

app.get('/objetos', (req, res) => {
	res.json(objetos);
});

app.post('/objetos', (req, res) => {
	const nuevo = req.body;
	if (!nuevo || !nuevo.nombre) {
		return res.status(400).json({ error: 'Falta nombre' });
	}
	nuevo.id = objetos.length ? objetos[objetos.length - 1].id + 1 : 1;
	objetos.push(nuevo);
	res.status(201).json(nuevo);
});

app.put('/objetos/:id', (req, res) => {
	const id = parseInt(req.params.id);
	const index = objetos.findIndex(obj => obj.id === id);
	if (index === -1) {
		return res.status(404).json({ error: 'No encontrado' });
	}
	const nuevo = req.body;
	if (!nuevo || !nuevo.nombre) {
		return res.status(400).json({ error: 'Falta nombre' });
	}
	nuevo.id = id;
	objetos[index] = nuevo;
	res.json(nuevo);
});

app.patch('/objetos/:id', (req, res) => {
	const id = parseInt(req.params.id);
	const index = objetos.findIndex(obj => obj.id === id);
	if (index === -1) {
		return res.status(404).json({ error: 'No encontrado' });
	}
	objetos[index] = { ...objetos[index], ...req.body, id };
	res.json(objetos[index]);
});

app.delete('/objetos/:id', (req, res) => {
	const id = parseInt(req.params.id);
	const index = objetos.findIndex(obj => obj.id === id);
	if (index === -1) {
		return res.status(404).json({ error: 'No encontrado' });
	}
	const eliminado = objetos.splice(index, 1);
	res.json(eliminado[0]);
});

app.listen(PORT, () => {
	console.log(`Servidor Express escuchando en puerto ${PORT}`);
});
