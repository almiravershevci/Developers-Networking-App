require('dotenv').config();

const express = require('express');
const cors = require('cors');

const firebaseAuth = require('./middleware/firebaseAuth');
const dashboardRoutes = require('./routes/dashboardRoutes');
const projectRoutes = require('./routes/projectRoutes');

const app = express();

app.use(express.json());
app.use(cors());

app.get('/', (_req, res) => {
  res.json({
    service: 'DevConnect Analytics API',
    project: process.env.FIREBASE_PROJECT_ID || 'developers-networking-app',
    routes: ['/api/dashboard/stats', '/api/projects'],
    auth: 'Firebase ID token (Authorization: Bearer)',
  });
});

app.use('/api/dashboard', firebaseAuth, dashboardRoutes);
app.use('/api/projects', firebaseAuth, projectRoutes);

const PORT = Number(process.env.PORT || 5000);
app.listen(PORT, () => {
  console.log(`DevConnect API listening on http://localhost:${PORT}`);
});
