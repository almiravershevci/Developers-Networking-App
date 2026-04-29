const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');

const app = express();

app.use(express.json());
app.use(cors());

// Importimi i të gjitha rrugëve
const userRoutes = require('./routes/userRoutes');
const projectRoutes = require('./routes/projectRoutes');
const dashboardRoutes = require('./routes/dashboardRoutes');
const authMiddleware = require('./middleware/auth'); // Importo middleware-in

// Përdorimi i rrugëve
app.get('/', (req, res) => {
    res.send('Serveri i Developers Networking App po punon!');
});

app.use('/api/users', userRoutes);
app.use('/api/projects', projectRoutes);

// KËTU është rregullimi për Dashboard-in:
// E përdorim vetëm një herë dhe e mbrojmë me authMiddleware
app.use('/api/dashboard', authMiddleware, dashboardRoutes);

const PORT = 5000;
app.listen(PORT, () => {
    console.log(`Serveri u startua në http://localhost:${PORT}`);
});