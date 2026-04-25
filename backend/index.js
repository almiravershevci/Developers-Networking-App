const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');

const app = express();

// Lejon serverin të kuptojë të dhënat JSON që vijnë nga Android
app.use(express.json());
app.use(cors());

// Kjo është rruga e parë (Route) - Testimi
app.get('/', (req, res) => {
    res.send('Serveri i Developers Networking App po punon!');
});

// Vendos portën ku do të dëgjojë serveri
const PORT = 5000;
app.listen(PORT, () => {
    console.log(`Serveri u startua në http://localhost:${PORT}`);
});

const userRoutes = require('./routes/userRoutes');
app.use('/api/users', userRoutes);

const projectRoutes = require('./routes/projectRoutes');
app.use('/api/projects', projectRoutes);

const dashboardRoutes = require('./routes/dashboardRoutes');
app.use('/api/dashboard', dashboardRoutes);