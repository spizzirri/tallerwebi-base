const express = require('express');
const app = express();
const PORT = process.env.PORT || 3000;

app.use(express.json());

let objetos = [
	{ id: 1, nombre: 'Fabio', dni: '12345678' },
	{ id: 2, nombre: 'Roberto' , dni: '87654321' },
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

app.get('/deudores', (req, res) => {
	res.json(objetos);
});

app.get('/deudores/:dni', (req, res) => {
	const dni = req.params.dni;
	const objeto = objetos.find(obj => obj.dni === dni);
	if (!objeto) {
		return res.status(404).json({ error: 'No encontrado' });
	}
	res.json(objeto);
});

app.post('/deudores', (req, res) => {
	const nuevo = req.body;
	if (!nuevo || !nuevo.nombre) {
		return res.status(400).json({ error: 'Falta nombre' });
	}
	nuevo.id = objetos.length ? objetos[objetos.length - 1].id + 1 : 1;
	objetos.push(nuevo);
	res.status(201).json(nuevo);
});

app.put('/deudores/:id', (req, res) => {
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

app.patch('/deudores/:id', (req, res) => {
	const id = parseInt(req.params.id);
	const index = objetos.findIndex(obj => obj.id === id);
	if (index === -1) {
		return res.status(404).json({ error: 'No encontrado' });
	}
	objetos[index] = { ...objetos[index], ...req.body, id };
	res.json(objetos[index]);
});

app.delete('/deudores/:id', (req, res) => {
	const id = parseInt(req.params.id);
	const index = objetos.findIndex(obj => obj.id === id);
	if (index === -1) {
		return res.status(404).json({ error: 'No encontrado' });
	}
	const eliminado = objetos.splice(index, 1);
	res.json(eliminado[0]);
});

app.head('/deudores', (req, res) => {
	res.status(200).end();
});

app.options('/deudores', (req, res) => {
	res.set('Allow', 'GET,POST,PUT,PATCH,DELETE,HEAD,OPTIONS');
	res.status(200).end();
});

app.listen(PORT, () => {
	console.log(`Servidor Express escuchando en puerto ${PORT}`);
});
